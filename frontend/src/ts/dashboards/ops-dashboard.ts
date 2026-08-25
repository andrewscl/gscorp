import { ChartController } from "../types/chart-types";
import { initSiteCoverageBarChart } from "../charts/site-coverage/site-coverage-bar-chart";
import { initShiftCoverageDonuts } from "../charts/shifts/shifts-coverage-ingress-donut";
import { OpsDashboardService } from "../services/ops-dashboard-service";
import { OperationsDashboardResponse } from "../types/ops-dashboard-types";
import { mkChart } from "../lib/echarts-setup";

export async function init({ container }: { container: HTMLElement }) {
    const root = (container.querySelector('#ops-dashboard-root') as HTMLElement)
    // 1. Resolver elemento raiz y prevenir doble inicialización. 
                                                                    || container;
    if(!root || root.dataset.echartsInit === '1') return;
    root.dataset.echartsInit = '1';

    const dashboardService = new OpsDashboardService();

    // Registro de controladores mapeados a su extractor del Dto.
    const registeredCharts: {
        ctrl: ChartController;
        extractData: (res: OperationsDashboardResponse) => any
    } [] = [];

    // 2. ResizeObserver global único
    const ro = new ResizeObserver(() => {
        registeredCharts.forEach(({ ctrl }) => {
            try { ctrl.chart?.resize(); } catch {}
        });
    });

    /**
     * Helper para registrar un módulo de gráfico y definir qué lista del DTO consume
     */
    async function registerChart<TData>(
        initFn: (
            selector: string,
            options: { mkChart: typeof mkChart }
        ) => Promise<ChartController<TData> | null> | ChartController<TData> | null,
        selector: string,
        extractData: (res: OperationsDashboardResponse) => TData
    ) {
        try {
            const ctrl = await initFn(selector, { mkChart });
            if (!ctrl) return;
            registeredCharts.push({ ctrl, extractData });
            if (ctrl.container) {
                ro.observe(ctrl.container);
            }
        } catch (e) {
            console.error(`[OpsDashboard] Error al registrar '${selector}':`, e);
        }
    }

    // =========================================================================
    // 3. REGISTRO DE GRÁFICOS
    // =========================================================================

    // Gráfico de Barras de Cobertura (projectSiteShiftsCoverageSummary)
    await registerChart(
        initSiteCoverageBarChart, 
        '#site-coverage-bar-chart',
        (metrics) => metrics.projectSiteShiftsSummary);
    // Gráfico de Donuts por hora de ingreso () 
    await registerChart(
        initShiftCoverageDonuts,
        '#shifts-coverage-ingress-donut',
        (metrics) => metrics.shiftsCountLast24Hours
    );

    // =========================================================================
    // 4. PETICIÓN ÚNICA Y DISTRIBUCIÓN
    // =========================================================================
    async function loadAndDistributeMetrics() {
        try {
            const zoneId = Intl.DateTimeFormat().resolvedOptions().timeZone;
            
            // Petición consolidada a la API
            const metrics = await dashboardService.getOpsDashboardMetrics(zoneId);

            // Reparto de cada sub-array a su respectivo componente
            for (const { ctrl, extractData } of registeredCharts) {
                try {
                    const dataSegment = extractData(metrics);
                    await ctrl.render?.(dataSegment);
                } catch (e) {
                    console.warn(`[OpsDashboard] Error al renderizar gráfico:`, e);
                }
            }
        } catch (e) {
            console.error('[OpsDashboard] Error al obtener métricas consolidadas:', e);
        }
    }

// Carga inicial de datos
    await loadAndDistributeMetrics();

    // Refresco periódico cada 5 minutos
    const refreshIntervalMs = 5 * 60 * 1000;
    const refreshTimer = window.setInterval(loadAndDistributeMetrics, refreshIntervalMs);

    // =========================================================================
    // 5. CICLO DE VIDA (CLEANUP)
    // =========================================================================

    function cleanup() {
        clearInterval(refreshTimer);
        ro.disconnect();

        registeredCharts.forEach(({ ctrl }) => {
            try { ctrl.stop?.(); } catch {}
            try { ctrl.destroy?.(); } catch {}
            try { ctrl.chart?.dispose(); } catch {}
        });

        if (root) root.dataset.echartsInit = '0';
    }

    window.addEventListener('beforeunload', cleanup);
    document.addEventListener('fragment:will-unload', cleanup, { once: true });

}

// Auto-inicialización para soporte de fragmentos Thymeleaf / navegación SPA
if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            const root = document.getElementById('ops-dashboard-root');
            if (root) init({ container: root });
        });
} else {
        const root = document.getElementById('ops-dashboard-root');
        if (root) init({ container: root });
}