import { ChartController } from "../types/chart-types";
import { initSiteCoverageBarChart } from "../charts/site-coverage/site-coverage-bar-chart";
import { initShiftCoverageDonuts } from "../charts/shifts/shifts-coverage-ingress-donut";
import { OpsDashboardService } from "../services/ops-dashboard-service";
import { OperationsDashboardResponse } from "../types/ops-dashboard-types";
import { mkChart } from "../lib/echarts-setup";

const ROOT_SELECTOR = '#ops-dashboard-root';
const REFRESH_INTERVAL_MS = 5 * 60 * 1000;
const LOG = '[OpsDashboard]';

type RegisteredChart = {
    ctrl: ChartController<any>;
    extractData: (res: OperationsDashboardResponse) => unknown;
}

export async function init({ container }: { container: HTMLElement }) {
    const root = (container.querySelector(ROOT_SELECTOR) as HTMLElement | null ) ?? container;
    if(root.dataset.echartsInit === '1') return;
    root.dataset.echartsInit = '1';

    const dashboardService = new OpsDashboardService();
    const registeredCharts: RegisteredChart[] = [];
    const pending = new Set<ChartController<any>>();
    const ac = new AbortController();
    let disposed = false;
    let inFlight = false;
    let refreshTimer: number | undefined;
    let rafId: number | null = null;
    window.addEventListener('beforeunload', cleanup, { signal: ac.signal });
    document.addEventListener('fragment:will-unload', cleanup, { signal: ac.signal });

    const ro = new ResizeObserver((entries) => {
        for (const entry of entries) {
            const rc = registeredCharts.find(r => r.ctrl.container === entry.target);
            if (rc) pending.add(rc.ctrl);
        }
        if (rafId !== null) return;
        rafId = requestAnimationFrame(() => {
            rafId = null;
            pending.forEach(c => {
                try {
                    c.chart?.resize();
                } catch (err) {
                    console.debug(`${LOG} resize falló`, err);
                }
            });
            pending.clear();
        });
    });

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
            if (disposed) { try { ctrl?.destroy?.(); ctrl?.stop?.(); ctrl?.chart?.dispose(); } catch {} return; }
            if (!ctrl) return;
            registeredCharts.push({ ctrl, extractData });
            if (ctrl.container) ro.observe(ctrl.container);
        } catch (e) {
            console.debug(`${LOG} dispose tardio'${selector}':`, e);
        }
    }

    function cleanup() {
        if (disposed) return;
        disposed = true;
        window.clearInterval(refreshTimer);
        if(rafId !== null) { cancelAnimationFrame(rafId); rafId = null; }
        ro.disconnect();
        ac.abort();
        pending.clear();

        registeredCharts.forEach(({ ctrl }) => {
            try { ctrl.stop?.(); } catch (e) { console.debug(`${LOG} stop`, e); }
            try { ctrl.destroy?.(); } catch (e) { console.debug(`${LOG} destroy`, e); }
            try { ctrl.chart?.dispose(); } catch (e) { console.debug(`${LOG} dispose`, e); }
        });
        registeredCharts.length = 0;
        root.dataset.echartsInit = '0';
    }

    try {
        await Promise.all([
            registerChart(initSiteCoverageBarChart, '#site-coverage-bar-chart',
                (metrics) => metrics.projectSiteShiftsSummary),
            registerChart(initShiftCoverageDonuts, '#shifts-coverage-ingress-donut',
                (metrics) => metrics.shiftsCountLast24Hours),
        ]);

        const zoneId = Intl.DateTimeFormat().resolvedOptions().timeZone;

        async function loadAndDistributeMetrics() {
            if (inFlight || disposed) return;
            inFlight = true;
            try {
                const metrics = await dashboardService.getOpsDashboardMetrics(zoneId);
                if (disposed) return;
                const results = await Promise.allSettled(
                    registeredCharts.map(({ ctrl, extractData}) => 
                        ctrl.render?.(extractData(metrics)))
                );
                results.forEach(r => {
                    if (r.status === 'rejected')
                        console.warn(`${LOG} Error al renderizar gráfico:`, r.reason);
                });
            } catch (e) {
                console.error(`${LOG} Error al obtener métricas consolidadas:`, e);
            } finally {
                inFlight = false;
            }
        }
        const startTimer = () => {
            refreshTimer = window.setInterval(loadAndDistributeMetrics, REFRESH_INTERVAL_MS);
        };
        await loadAndDistributeMetrics();
        if (disposed) return;
        startTimer();
        document.addEventListener('visibilitychange', () => {
            if(document.visibilityState === 'hidden') {
                window.clearInterval(refreshTimer);
            } else if(!disposed) {
                void loadAndDistributeMetrics();
                window.clearInterval(refreshTimer);
                startTimer();
            }
        }, { signal: ac.signal });
    } catch (e) {
        console.error(`${LOG} Error durante la inicialización:`, e);
        cleanup();
    }
}

function boot() {
    const root = document.querySelector<HTMLElement>(ROOT_SELECTOR);
    if (root) void init({ container: root });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot, { once: true });
} else {
    boot();
}
