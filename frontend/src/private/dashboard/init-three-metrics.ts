import { initHourlyMetricChart } from './hourly-metric';

export function initThreeMetrics(
  container: HTMLElement,
  date?: string,
  options?: { refreshMs?: number }
) {
  const refreshMs = options?.refreshMs ?? 0;

  // Inicializa 3 widgets
  initHourlyMetricChart(container, 'attendance', 'Asistencias', '#chart-hourly-attendance', date, { color: '#06b6d4', forecastColor: '#0891b2' });
  initHourlyMetricChart(container, 'rounds', 'Rondas', '#chart-hourly-rounds', date, { color: '#10b981', forecastColor: '#047857' });
  initHourlyMetricChart(container, 'visits', 'Visitas', '#chart-hourly-visits', date, { color: '#f59e0b', forecastColor: '#b45309' });

  // Opcional: refresco periódico re-inicializando (fácil, aunque no óptimo)
  let intervalId: number | undefined;
  if (refreshMs && refreshMs > 0) {
    intervalId = window.setInterval(() => {
      initHourlyMetricChart(container, 'attendance', 'Asistencias', '#chart-hourly-attendance', date, { color: '#06b6d4', forecastColor: '#0891b2' });
      initHourlyMetricChart(container, 'rounds', 'Rondas', '#chart-hourly-rounds', date, { color: '#10b981', forecastColor: '#047857' });
      initHourlyMetricChart(container, 'visits', 'Visitas', '#chart-hourly-visits', date, { color: '#f59e0b', forecastColor: '#b45309' });
    }, refreshMs);
  }

  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { if (intervalId) window.clearInterval(intervalId); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}