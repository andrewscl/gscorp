import { echarts } from '../../lib/echarts-setup';
import { fetchWithAuth } from './api/api';

type HourlyPoint = { hour: string; value: number; forecast: number };

/**
 * Renderiza un chart por hora para una sola métrica.
 * - metricKey: 'attendance' | 'rounds' | 'visits'
 * - displayName: etiqueta para leyenda/tooltip
 * - elId: id selector del div donde renderizar (p. ej. '#chart-hourly-attendance')
 */
export async function initHourlyMetricChart(
  container: HTMLElement,
  metricKey: 'attendance' | 'rounds' | 'visits',
  displayName: string,
  elId: string,
  date?: string,
  opts?: { color?: string; forecastColor?: string; height?: number }
) {
  const el = container.querySelector(elId) as HTMLDivElement | null;
  if (!el) {
    console.warn(`[hourly metric] ${elId} no existe`);
    return;
  }

  // Fetch data
  let raw: any[] = [];
  try {
    const q = date ? `?date=${encodeURIComponent(date)}` : '';
    const res = await fetchWithAuth(`/api/dashboard/today/hourly${q}`);
    if (res.ok) raw = await res.json();
    else {
      console.warn('[hourly metric] API status:', res.status, await res.text().catch(()=>''));
    }
  } catch (e) {
    console.warn('[hourly metric] fetch error', e);
  }

  // Guarantee 24 hours as "00".."23"
  const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
  const map = new Map(raw.map(r => [r.hour, r]));
  const points: HourlyPoint[] = hours.map(h => {
    const r = map.get(h);
    return {
      hour: h,
      value: r ? Number(r[metricKey] ?? 0) : 0,
      forecast: r ? Number(r[metricKey + 'Forecast'] ?? 0) : 0
    };
  });

  const values = points.map(p => p.value);
  const forecastValues = points.map(p => p.forecast);
  const hasData = values.some(v => v > 0) || forecastValues.some(v => v > 0);

  const color = opts?.color ?? '#3b82f6';
  const forecastColor = opts?.forecastColor ?? '#60a5fa';

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 6 },
    grid: { left: 40, right: 16, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: hours, axisLabel: { formatter: (v: string) => v + 'h' } },
    yAxis: { type: 'value', minInterval: 1 },
    series: hasData ? [
      { name: `${displayName} (real)`, type: 'bar', data: values, itemStyle: { color } },
      { name: `${displayName} (forecast)`, type: 'line', smooth: true, data: forecastValues, lineStyle: { type: 'dashed', color: forecastColor }, symbol: 'circle', symbolSize: 6 }
    ] : [],
    graphic: hasData ? { elements: [] } : {
      elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: 'Sin datos', fill: '#9ca3af', fontSize: 14 } }]
    },
    toolbox: { show: true, feature: { saveAsImage: {} } }
  });

  // ResizeObserver para redimensionar el chart cuando cambia el contenedor
  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);

  // Cleanup: cuando el fragment SPA se vaya limpiar listeners y chart
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}