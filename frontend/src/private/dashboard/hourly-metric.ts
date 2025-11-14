import { echarts } from '../../lib/echarts-setup';
import { fetchWithAuth } from './api/api';

export type Refresher = {
  chart: echarts.ECharts;
  refresh: (raw: HourlyPointRaw[]) => void;
  destroy: () => void;
};

export type HourlyPointRaw = {
  hour: string;
  attendance?: number;
  attendanceForecast?: number;
  rounds?: number;
  roundsForecast?: number;
  visits?: number;
  visitsForecast?: number;
};

type HourlyPoint = { hour: string; value: number; forecast: number };

/**
 * Crea y devuelve un chart por hora para una métrica, junto con un refresher para actualizarlo.
 * - metricKey: 'attendance' | 'rounds' | 'visits'
 * - displayName: etiqueta para leyenda/tooltip
 * - elId: selector del div donde renderizar (p. ej. '#chart-hourly-attendance')
 * - date: si se provee, la función hará un fetch inicial a /api/dashboard/today/hourly?date=...
 *
 * Retorna Promise<Refresher | null>
 */
export async function initHourlyMetricChart(
  container: HTMLElement,
  metricKey: 'attendance' | 'rounds' | 'visits',
  displayName: string,
  elId: string,
  date?: string,
  opts?: { color?: string; forecastColor?: string; height?: number }
): Promise<Refresher | null> {
  const el = container.querySelector(elId) as HTMLDivElement | null;
  if (!el) {
    console.warn(`[hourly metric] ${elId} no existe`);
    return null;
  }

  const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
  const color = opts?.color ?? '#3b82f6';
  const forecastColor = opts?.forecastColor ?? '#60a5fa';

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });

  // Inicializar con series vacías / indicador de carga
  const emptySeries = hours.map(() => 0);
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 6 },
    grid: { left: 40, right: 16, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: hours, axisLabel: { formatter: (v: string) => v + 'h' } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: `${displayName} (real)`, type: 'bar', data: emptySeries, itemStyle: { color } },
      { name: `${displayName} (forecast)`, type: 'line', smooth: true, data: emptySeries, lineStyle: { type: 'dashed', color: forecastColor }, symbol: 'circle', symbolSize: 6 }
    ],
    graphic: { elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: 'Cargando...', fill: '#9ca3af', fontSize: 14 } }] },
    toolbox: { show: true, feature: { saveAsImage: {} } }
  });

  // Función que normaliza raw[] y actualiza series en el chart
  function applyRaw(raw: HourlyPointRaw[]) {
    const map = new Map((raw || []).map(r => [String(r.hour).padStart(2, '0'), r]));
    const values = hours.map(h => Number((map.get(h) && (map.get(h) as any)[metricKey]) ?? 0));
    const forecastValues = hours.map(h => Number((map.get(h) && (map.get(h) as any)[metricKey + 'Forecast']) ?? 0));
    const hasData = values.some(v => v > 0) || forecastValues.some(v => v > 0);

    chart.setOption({
      xAxis: { data: hours },
      series: [
        { name: `${displayName} (real)`, type: 'bar', data: values, itemStyle: { color } },
        { name: `${displayName} (forecast)`, type: 'line', smooth: true, data: forecastValues, lineStyle: { type: 'dashed', color: forecastColor }, symbol: 'circle', symbolSize: 6 }
      ],
      graphic: hasData ? { elements: [] } : {
        elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: 'Sin datos', fill: '#9ca3af', fontSize: 14 } }]
      }
    });
  }

  // ResizeObserver para redimensionar el chart cuando cambia el contenedor
  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);

  // Cleanup/destroy
  function destroy() {
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
    // eliminar listener en caso de que exista
    try { document.removeEventListener('fragment:will-unload', onUnload as EventListener); } catch {}
  }

  // Handler para unload SPA (también permite cleanup si se utiliza solo destroy)
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    destroy();
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });

  // Adjuntar cleanup accesible externamente
  (chart as any).__hourly_metric_cleanup = destroy;

  // Fetch inicial si se pasa date (mantengo tu comportamiento previo)
  if (date) {
    try {
      const q = `?date=${encodeURIComponent(date)}`;
      const res = await fetchWithAuth(`/api/dashboard/today/hourly${q}`);
      if (res.ok) {
        const raw: HourlyPointRaw[] = await res.json().catch(() => []);
        applyRaw(raw);
      } else {
        chart.setOption({ graphic: { elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: 'Error de datos', fill: '#9ca3af', fontSize: 14 } }] } });
      }
    } catch (e) {
      chart.setOption({ graphic: { elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: 'Error de datos', fill: '#9ca3af', fontSize: 14 } }] } });
      console.warn('[hourly metric] fetch error', e);
    }
  }

  // Retornar el refresher para actualización en caliente
  const refresher: Refresher = {
    chart,
    refresh: (raw: HourlyPointRaw[]) => applyRaw(raw),
    destroy
  };

  return refresher;
}