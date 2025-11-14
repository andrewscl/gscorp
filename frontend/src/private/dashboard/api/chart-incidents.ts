import { echarts } from '../../../lib/echarts-setup';
import { fetchWithAuth } from './api';

type Point = { x: string; y: number };

/**
 * Inicializa el gráfico de incidentes por día usando ECharts.
 * @param container - Elemento raíz donde buscar el div del gráfico
 * @param from - Fecha de inicio en formato YYYY-MM-DD
 * @param to - Fecha de término en formato YYYY-MM-DD
 */
export async function initIncidentsChart(
  container: HTMLElement,
  from: string,
  to: string
) {
  const el = container.querySelector('#chart-inc') as HTMLDivElement;
  if (!el) {
    console.warn('[incidents chart] #chart-inc no existe');
    return;
  }

  let data: Point[] = [];
  try {
    const res = await fetchWithAuth(`/api/incidents/series?from=${from}&to=${to}`);
    if (res.ok) data = await res.json();
    else console.warn('[incidents chart] API status:', res.status, await res.text().catch(()=>'')); // debug
  } catch (e) {
    console.warn('[incidents chart] fetch error', e);
  }

  const labels = data.map(p => p.x);
  const values = data.map(p => p.y);
  const hasData = values.some(v => Number(v) > 0);

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { show: true },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', boundaryGap: false, data: labels },
    yAxis: { type: 'value' },
    series: [{
      name: 'Incidentes',
      type: 'bar',
      data: values,
      color: '#f43f5e'
    }],
    graphic: hasData ? { elements: [] } : {
      elements: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: { text: 'Sin datos', fill: '#9ca3af', fontSize: 14 }
      }]
    }
  });

  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);

  // Limpieza al salir del fragmento SPA
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}