import { mkChart } from '../lib/echarts-setup';
import { initAttendanceHourlyChart } from '../charts/attendances/attendance-hourly-chart';
import { initAttendanceDailyChart } from '../charts/attendances/attendance-daily-chart';
import { initVisitsDailyChart } from '../charts/site-visits/site-visit-daily-chart';
import { initVisitHourlyChart } from '../charts/site-visits/site-visit-hourly-chart';
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

  // ResizeObserver creado antes para que registerChart pueda observar inmediatamente
  const ro = new ResizeObserver(() => {
    controllers.forEach(c => {
      try { c.chart?.resize?.(); } catch {}
    });
  });

  // helper para inicializar y registrar cualquier chart module
  async function registerChart(initFn: (...args: any[]) => ChartController | Promise<ChartController>, ...args: any[]) {
    try {
      const ctrl = await initFn(...args);
      if (!ctrl) return;
      // primer render si existe
      try { await ctrl.render?.(); } catch (e) { console.warn('Initial render failed for chart', e); }
      controllers.push(ctrl);
      // observar el contenedor para resize si aplica
      try { if (ctrl.container) ro.observe(ctrl.container); } catch (e) { /* ignore */ }
    } catch (e) {
      console.error('Error initializing chart', e);
    }
  }

  // --- Attendance hourly chart ---
  await registerChart(initAttendanceHourlyChart, '#chart-hourly-attendance', {
    days: 1,
    mkChart,
    fetchWithTimeout
  });

  // --- Visit hourly chart ---
  await registerChart(initVisitHourlyChart, '#chart-hourly-visit', {
    days: 1,
    mkChart,
    fetchWithTimeout
  });

  // --- Attendance daily chart ---
  await registerChart(initAttendanceDailyChart, '#chart-daily-attendance', {
    days: 7,
    mkChart,
    fetchWithTimeout
  });

  // --- Visits daily chart ---
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