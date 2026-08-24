import { ChartController } from "../types/chart-types";
import { initSiteCoverageBarChart } from "../charts/site-coverage/site-coverage-bar-chart";

export async function init ({ container }: { container: HTMLElement }){
    // 1. Resolver el elemento raíz del dashboard y evitar doble inicialización
    const root = (container.querySelector('#admin-dashboard-root') as HTMLElement) || container;
    if (!root || root.dataset.echartsInit === '1') return;
    root.dataset.echartsInit = '1';

    const controllers: ChartController[] = [];

    // 2. Un único ResizeObserver eficiente para todos los canvas del dashboard
    const ro = new ResizeObserver(() => {
        controllers.forEach(c => {
        try { c.chart?.resize(); } catch {}
        });
    });

    /**
     * Helper simplificado para registrar módulos de gráficos.
     * Cada módulo interno ahora importa sus propios Services y Mappers directamente.
     */
    async function registerChart<TConfig>(
        initFn: (selector: string, config?: TConfig) => Promise<ChartController | null>,
        selector: string,
        config?: TConfig
    ) {
        try {
        const ctrl = await initFn(selector, config);
        if (!ctrl) return;
        // Ejecutar primer renderizado
        try { 
            await ctrl.render(); 
        } catch (e) { 
            console.warn(`[AdminDashboard] Render inicial falló para '${selector}':`, e); 
        }
        controllers.push(ctrl);
        // Registrar el contenedor DOM en el ResizeObserver global
        if (ctrl.container) {
            ro.observe(ctrl.container);
        }
        } catch (e) {
        console.error(`[AdminDashboard] Error al inicializar el gráfico '${selector}':`, e);
        }
    }

    // =========================================================================
    // MONTAJE DE GRÁFICOS (Consumiendo sus propios Services internamente)
    // =========================================================================

    // Gráfico de Barras de Cobertura por Sitio
    await registerChart(initSiteCoverageBarChart, '#site-coverage-bar-chart');

    // Gráfico Donut de Ingresos/Turnos
    // await registerChart(initShiftCoverageDonuts, '#shifts-coverage-ingress-donut', { days: 1 });

    // =========================================================================
    // REFRESCO PERIÓDICO Y CICLO DE VIDA (CLEANUP)
    // =========================================================================

    // Polling automático cada 5 minutos
    const refreshIntervalMs = 5 * 60 * 1000;
    const refreshTimer = window.setInterval(() => {
        controllers.forEach(async (c) => {
        try { 
            await c.render(); 
        } catch (err) { 
            console.warn('[AdminDashboard] Error en actualización periódica:', err); 
        }
        });
    }, refreshIntervalMs);

    // Función de limpieza única para prevenir memory leaks
    function cleanup() {
        clearInterval(refreshTimer);
        ro.disconnect();

        controllers.forEach(c => {
        try { c.stop?.(); } catch {}
        try { c.destroy?.(); } catch {}
        try { c.chart?.dispose(); } catch {}
        });

        if (root) root.dataset.echartsInit = '0';
    }

    // Suscripción a eventos de desmontaje
    window.addEventListener('beforeunload', cleanup);
    document.addEventListener('fragment:will-unload', cleanup, { once: true });

}