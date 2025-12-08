import { mkChart } from '../lib/echarts-setup';
import { initVisitsDailyChart } from '../charts/site-visits/site-visit-daily-chart';
import { initAttendanceDailyChart } from '../charts/attendances/attendance-daily-chart';
import { fetchWithTimeout } from '../utils/api';


type ChartController = {
  render?: () => Promise<void> | void;
  destroy?: () => void;
  stop?: () => void;
  chart?: any;
  container?: HTMLElement | null;
};


export async function init({ container }: { container: HTMLElement }) {

  const root = (container.querySelector('#client-dashboard-root') as HTMLElement) || container;
  if (!root || root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';

  const controllers: ChartController[] = [];

  // helper para inicializar y registrar cualquier chart module
  async function registerChart(initFn: (...args: any[]) => ChartController | Promise<ChartController>, ...args: any[]) {
    try {
      const ctrl = await initFn(...args);
      if (!ctrl) return;
      // primer render si existe
      try { await ctrl.render?.(); } catch (e) { console.warn('Initial render failed for chart', e); }
      controllers.push(ctrl);
    } catch (e) {
      console.error('Error initializing chart', e);
    }
  }

  // --- daily attendance ---
  await registerChart(initAttendanceDailyChart, '#chart-daily-attendance', {
      days: 7,
      mkChart,
      fetchWithTimeout
  });

  // --- daily visit ---
  await registerChart(initVisitsDailyChart, '#chart-daily-visit', {
      days: 7,
      mkChart,
      fetchWithTimeout
  });


  // refresco global (llama render en cada controlador que tenga render)
  const refreshIntervalMs = 5 * 60 * 1000;
  const refreshTimer = window.setInterval(() => {
    controllers.forEach(async (c) => {
      try { await c.render?.(); } catch (err) { console.warn('Periodic render failed', err); }
    });
  }, refreshIntervalMs);


  // ResizeObserver para ajustar los charts (siempre que el controller tenga .chart con resize)
  const ro = new ResizeObserver(() => {
    controllers.forEach(c => {
      try { c.chart?.resize?.(); } catch {}
    });
  });
  // observa los contenedores si existen
  controllers.forEach(c => {
    try { if (c.container) ro.observe(c.container); } catch {}
  });

  // cleanup único
  function cleanup() {
    try { clearInterval(refreshTimer); } catch {}
    try { ro.disconnect(); } catch {}
    controllers.forEach(c => {
      try { c.stop?.(); } catch {}
      try { c.destroy?.(); } catch {}
      try { c.chart?.dispose?.(); } catch {}
    });
    try { root.dataset.echartsInit = '0'; } catch {}
  }

  // registra sólo un beforeunload + fragment unload
  window.addEventListener('beforeunload', cleanup);
  document.addEventListener('fragment:will-unload', cleanup, { once: true });


}

// Auto-init si insertas el script directamente en el fragmento
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    const root = document.getElementById('client-dashboard-root');
    if (root) init({ container: root });
  });
} else {
  const root = document.getElementById('client-dashboard-root');
  if (root) init({ container: root });
}