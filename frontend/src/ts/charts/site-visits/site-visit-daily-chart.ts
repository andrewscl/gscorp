// Visits daily chart module (last N days) — extracted from client-dashboard
// - Exports initVisitsDailyChart(selector, opts) -> { render, destroy, chart, container }
// - Reuses shared helpers and mkChart from lib/echarts-setup
import { fetchWithTimeout } from '../../utils/api';
import {
  normalizeToPoints,
  aggregateByDate,
  valuesForLabels,
  buildLastNDatesIso,
  shortLabelFromIso,
  getMonthFormatter
} from '../../utils/chart-utils';
import { safeSetNoData } from '../../utils/chart-uiutils';
import { mkChart as defaultMkChart } from '../../lib/echarts-setup';

export type VisitsChartOptions = {
  days?: number;
  tz?: string;
  fetchWithTimeout?: typeof fetchWithTimeout;
  mkChart?: (el: HTMLElement | null) => any;
  root?: Document | HTMLElement;
  apiBase?: string;
};

export function initVisitsDailyChart(containerSelector = '#chart-visits',
                                            opts: VisitsChartOptions = {}) {
  const root = (opts.root ?? document) as Document;
  const el = root.querySelector(containerSelector) as HTMLDivElement | null;

  const mkChartFn = opts.mkChart ?? defaultMkChart;
  const fetchFn = opts.fetchWithTimeout ?? fetchWithTimeout;
  const monthFormatter = getMonthFormatter();
  const days = opts.days ?? 7;
  const apiBase = opts.apiBase ?? '';

  const ch = mkChartFn ? mkChartFn(el) : null;

  async function render() {
    try {
      const tz = opts.tz ?? Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC';
      const params = new URLSearchParams({ days: String(days), tz });
      const urlActual = `${apiBase}/api/site-supervision-visits/series?${params.toString()}`;
      const urlForecast = `${apiBase}/api/forecasts/forecast-series?${params.toString()}`;

      const [resActual, resForecast] = await Promise.all([
        fetchFn ? fetchFn(urlActual, {}, 15000, true).catch((e: any) => { console.warn(e); return null; }) : fetch(urlActual).catch(() => null),
        fetchFn ? fetchFn(urlForecast, {}, 15000, true).catch((e: any) => { console.warn(e); return null; }) : fetch(urlForecast).catch(() => null)
      ]);

      const parseOrEmpty = async (r: Response | null) => {
        if (!r) return [];
        if (!r.ok) return [];
        return await r.json().catch(() => []);
      };

      const [dataActual, dataForecast] = await Promise.all([parseOrEmpty(resActual), parseOrEmpty(resForecast)]);

      const bothEmpty = (!Array.isArray(dataActual) || dataActual.length === 0) &&
                        (!Array.isArray(dataForecast) || dataForecast.length === 0);
      if (bothEmpty) {
        safeSetNoData(ch, el, 'Sin datos');
        return;
      }

      const normActual = normalizeToPoints(dataActual);
      const normForecast = normalizeToPoints(dataForecast);

      const labels = buildLastNDatesIso(days);

      const mActual = aggregateByDate(normActual);
      const mForecast = aggregateByDate(normForecast);

      const valuesActual = valuesForLabels(labels, mActual);
      const valuesForecast = valuesForLabels(labels, mForecast);

      if (!ch) {
        safeSetNoData(ch, el, 'Sin datos');
        return;
      }

      ch.clear();
      ch.setOption({
        legend: { data: ['Visitas', 'Forecast'], top: 6 },
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            if (!Array.isArray(params) || params.length === 0) return '';
            const first = params[0];
            let rawDate: any = first.axisValue ?? (typeof first.dataIndex === 'number' ? labels[first.dataIndex] : undefined);
            if (!rawDate && first.axisValueLabel) rawDate = first.axisValueLabel;
            const displayDate = shortLabelFromIso(String(rawDate ?? ''), monthFormatter);
            const lines = params.map((p: any) => `${p.marker} ${p.seriesName}: ${p.value ?? 0}`);
            return `<b>${displayDate}</b><br/>${lines.join('<br/>')}`;
          }
        },
        grid: { left: 40, right: 16, top: 48, bottom: 36 },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: labels,
          axisLabel: {
            rotate: 0,
            formatter: (value: string) => shortLabelFromIso(value, monthFormatter)
          }
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: 'Visitas',
            type: 'line',
            smooth: true,
            areaStyle: {},
            data: valuesActual,
            color: '#0ea5e9',
            showSymbol: false,
            lineStyle: { width: 2 }
          },
          {
            name: 'Forecast',
            type: 'line',
            smooth: true,
            areaStyle: { opacity: 0.12 },
            data: valuesForecast,
            color: '#f59e0b',
            showSymbol: false,
            lineStyle: { width: 2, type: 'dashed' }
          }
        ],
        graphic: (valuesActual.concat(valuesForecast)).some(v => Number(v) > 0) ? { elements: [] } : undefined
      });

      // Donut (optional): keep using existing DOM id '#donut-visit'
      const elDonutVisit = root.querySelector('#donut-visit') as HTMLDivElement | null;
      if (elDonutVisit) {
        const chDonut = mkChartFn ? mkChartFn(elDonutVisit) : null;
        if (chDonut) {
          const sumActual = valuesActual.reduce((s, v) => s + (Number(v) || 0), 0);
          const sumForecast = valuesForecast.reduce((s, v) => s + (Number(v) || 0), 0);
          const hasMeta = sumForecast > 0;
          const percentage = hasMeta ? Math.round((sumActual / sumForecast) * 100) : 0;
          const pctForSeries = hasMeta ? Math.min(100, Math.max(0, percentage)) : 100;
          const seriesData = !hasMeta
            ? [{ value: 1, name: 'Sin meta', itemStyle: { color: '#E5E7EB' } }]
            : [
                { value: pctForSeries, name: 'Cumplido', itemStyle: { color: '#10B981' } },
                { value: 100 - pctForSeries, name: 'Pendiente', itemStyle: { color: '#E5E7EB' } }
              ];
          chDonut.setOption({
            tooltip: {
              trigger: 'item',
              formatter: (p: any) => {
                if (!hasMeta) return 'Meta no definida';
                if (p && p.name === 'Cumplido') {
                  return `${p.marker} ${p.seriesName}: ${sumActual} / ${sumForecast} (${percentage}%)`;
                }
                if (p && p.name === 'Pendiente') {
                  return `${p.marker} Pendiente: ${Math.max(0, sumForecast - sumActual)}`;
                }
                return '';
              }
            },
            series: [{
              name: 'Cumplimiento Visitas',
              type: 'pie',
              radius: ['62%', '82%'],
              avoidLabelOverlap: false,
              hoverAnimation: false,
              label: {
                show: true,
                position: 'center',
                formatter: hasMeta ? `${percentage}%` : '—',
                fontSize: 16,
                fontWeight: 700,
                color: '#485572ff'
              },
              labelLine: { show: false },
              data: seriesData
            }]
          });
        }
      }

      const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);
      if (!anyPositive) safeSetNoData(ch, el, 'Sin visitas');

    } catch (err: any) {
      if (err?.name === 'AbortError') {
        console.debug('Visits/Forecast fetch aborted (timeout or navigation)', err);
      } else {
        console.error('Error obtaining visits series/forecast', err);
      }
      safeSetNoData(ch, el, 'Error de datos');
    }
  }

  function destroy() {
    try {
      if (ch && typeof ch.dispose === 'function') ch.dispose();
      if (el) {
        const ph = el.querySelector('.chart-placeholder');
        if (ph) ph.remove();
      }
    } catch (e) { /* ignore */ }
  }

  return { render, destroy, chart: ch, container: el };
}