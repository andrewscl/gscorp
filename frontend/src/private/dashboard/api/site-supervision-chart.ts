import { echarts } from '../../../lib/echarts-setup';
import { fetchWithAuth } from './api';

type SiteVisitCountDto = {
  siteId: number;
  siteName: string;
  count: number;
};

export async function initVisitsBySiteChart(
  container: HTMLElement,
  from: string,
  to: string,
  clientId: number | string
) {
  const el = container.querySelector('#chart-visit-site') as HTMLDivElement;
  if (!el) {
    console.warn('[visits by site chart] #chart-visit-site no existe');
    return;
  }
  let data: SiteVisitCountDto[] = [];
  try {
    const res = await fetchWithAuth(
      `/api/site-supervision-visits/series-by-site?clientId=${clientId}&from=${from}&to=${to}`
    );
    if (res.ok) data = await res.json();
    else console.warn('[visits by site chart] API status:', res.status, await res.text().catch(()=>'')); // debug
  } catch (e) {
    console.warn('[visits by site chart] fetch error', e);
  }

  const labels = data.map(item => item.siteName);
  const values = data.map(item => item.count);

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });
  chart.setOption({
    tooltip: { trigger: 'item' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{
      name: 'Visitas',
      type: 'bar',
      data: values,
      itemStyle: { color: '#0ea5e9' }
    }]
  });

  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);

  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}