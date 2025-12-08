import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, DatasetComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// Registrar los componentes que se usan en la app
echarts.use([
  LineChart, BarChart, PieChart,
  GridComponent, TooltipComponent, LegendComponent, DatasetComponent,
  CanvasRenderer
]);

/**
 * Inicializa o recupera la instancia de ECharts asociada al DOM element.
 * Devuelve null si el elemento no existe o la inicialización falla.
 */
export function mkChart(el: HTMLElement | null): echarts.ECharts | null {
  if (!el) return null;
  try {
    // reusar instancia si ya existe en ese DOM
    const existing = echarts.getInstanceByDom(el);
    if (existing) return existing;
    return echarts.init(el, undefined, { renderer: 'canvas' });
  } catch (e) {
    // no romper la ejecución si echarts falla (por ejemplo en SSR)
    // eslint-disable-next-line no-console
    console.warn('mkChart: echarts.init failed', e);
    return null;
  }
}

/** Dispose seguro de una instancia creada en el DOM dado (si existe) */
export function disposeChart(el: HTMLElement | null) {
  if (!el) return;
  try {
    const inst = echarts.getInstanceByDom(el);
    if (inst) inst.dispose();
  } catch (e) {
    // eslint-disable-next-line no-console
    console.warn('disposeChart failed', e);
  }
}

export { echarts };