import { mkChart } from '../lib/echarts-setup';
import { initVisitsDailyChart } from '../charts/site-visits/site-visit-daily-chart';
import { fetchWithTimeout } from '../utils/api';


export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#client-dashboard-root') as HTMLElement) || container;
  if (!root || root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';


  // Initialize site visit daily chart
  const visits = initVisitsDailyChart('#chart-visit', {
      days: 7,
      mkChart,
      fetchWithTimeout
  });

  // Only render if the chart module found its container
  if (visits?.container) {
    try {
      await visits.render();
    } catch (err) {
      console.error('Visits chart initial render failed', err);
      // show no-data fallback (the module uses safeSetNoData internally)
    }
  } else {
    // No container -> nothing to render; optionally log
    console.debug('Visits daily chart container not found, skipping render');
  }

  // schedule refresh / teardown
  const interval = Number(window.setInterval(() => {
    try {
      visits?.render?.();
    } catch (err) {
      console.warn('Visits chart periodic render failed', err);
    }
  }, 5 * 60 * 1000));

  const cleanup = () => {
    try { clearInterval(interval); } catch {}
    try { visits?.destroy?.(); } catch (e) { console.warn('Error destroying visits chart', e); }
    try { root.dataset.echartsInit = '0'; } catch {}
  };

  window.addEventListener('beforeunload', cleanup);
  // If your app fires a custom fragment unload event, also listen it
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