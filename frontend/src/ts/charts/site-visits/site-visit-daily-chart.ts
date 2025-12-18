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

  metric?: string;
  siteId?: number | null;
  projectId?: number | null;
  forecastPath?: string | null;
};

export function initVisitsDailyChart(containerSelector = '#chart-daily-visit',
                                            opts: VisitsChartOptions = {}) {
  const root = (opts.root ?? document) as Document;
  const el = root.querySelector(containerSelector) as HTMLDivElement | null;

  const mkChartFn = opts.mkChart ?? defaultMkChart;
  const fetchFn = opts.fetchWithTimeout ?? fetchWithTimeout;
  const monthFormatter = getMonthFormatter();
  const days = opts.days ?? 7;
  const apiBase = opts.apiBase ?? '';

  const ch = mkChartFn ? mkChartFn(el) : null;

  // Helper: add param only if value is not null/undefined/empty/'undefined'
  function safeAddParam(params: URLSearchParams, key: string, val: any) {
    if (val === null || val === undefined) return;
    if (typeof val === 'string') {
      const s = val.trim();
      if (s === '' || s === 'undefined') return;
    }
    params.set(key, String(val));
  }

  async function render() {
    try {
      const tz = opts.tz ?? Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC';
      const paramsActual = new URLSearchParams({ days: String(days), tz });
      const urlActual = `${apiBase}/api/site-supervision-visits/series?${paramsActual.toString()}`;

      //Forecast params: metric, siteId, projectId
      const forecastMetric = opts.metric ?? 'VISITS';
      const willFetchForecast = typeof opts.forecastPath === 'undefined' ? true : !!opts.forecastPath;
      let urlForecast = null;
      if(willFetchForecast) {
        const paramsForecast = new URLSearchParams({
                days: String(days),
                tz,
                metric: forecastMetric
              });
              
        safeAddParam(paramsForecast, 'siteId', opts.siteId);
        safeAddParam(paramsForecast, 'projectId', opts.projectId);

        const forecastBase = opts.forecastPath ?? `${apiBase}/api/forecasts/forecast-series`;
        urlForecast = `${forecastBase}?${paramsForecast.toString()}`;
      }

      const [resActual, resForecast] = await Promise.all([
        fetchFn ? fetchFn(urlActual, {}, 15000, true).catch((e: any) => { console.warn(e); return null; }) : fetch(urlActual).catch(() => null),
        // si urlForecast es null, no hacemos la petición
        urlForecast ? (fetchFn ? fetchFn(urlForecast, {}, 15000, true).catch((e: any) => { console.warn(e); return null; }) : fetch(urlForecast).catch(() => null)) : Promise.resolve(null)
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
          // evitar clipping por overflow del card
          appendToBody: true,
          confine: true,
          extraCssText: 'z-index: 99999; box-shadow: 0 4px 12px rgba(0,0,0,0.12);',
          // guía vertical para facilitar lectura
          axisPointer: { type: 'line', snap: false },
          // posición: cerca del cursor pero clamp dentro del viewport
          position: (pos: any, _params: any, _dom: any, _rect: any) => {
            try {
              const viewportW = window.innerWidth || document.documentElement.clientWidth;
              const viewportH = window.innerHeight || document.documentElement.clientHeight;
              const padding = 8;
              let x = Array.isArray(pos) ? pos[0] : (pos && pos.event ? pos.event.clientX : padding);
              let y = Array.isArray(pos) ? pos[1] : (pos && pos.event ? pos.event.clientY : padding);
              // elevar un poco sobre el cursor
              y = y - 36;
              // clamp dentro del viewport
              x = Math.min(Math.max(padding, x), viewportW - padding);
              y = Math.min(Math.max(padding, y), viewportH - padding);
              return [x, y];
            } catch (e) {
              return 'inside';
            }
          },
          // formatter robusto: maneja nulls y distintos shapes de params
          formatter: (params: any) => {
            if (!Array.isArray(params) || params.length === 0) return '';
            const first = params[0];
            const idx = (typeof first.dataIndex === 'number') ? first.dataIndex : undefined;
            const rawDate = (idx !== undefined && labels && labels[idx]) ? labels[idx] : (first.axisValue ?? first.axisValueLabel ?? '');
            const displayDate = (typeof shortLabelFromIso === 'function')
              ? shortLabelFromIso(String(rawDate ?? ''), monthFormatter)
              : String(rawDate ?? '');
            const lines = params.map((p: any) => {
              const value = (p?.value === null || p?.value === undefined) ? '—' : p.value;
              return `${p.marker ?? ''} ${p.seriesName ?? ''}: ${value}`;
            });
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

      // Donut
      const elDonutDailyVisit = root.querySelector('#donut-daily-visit') as HTMLDivElement | null;
      if (elDonutDailyVisit) {
        const chDonut = mkChartFn ? mkChartFn(elDonutDailyVisit) : null;
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

          // actualizar totales visibles en la card (Total semana / Meta)
          // colocar justo después de calcular sumActual, sumForecast, hasMeta, percentage, seriesData
          const totalEl = root.querySelector('#total-daily-visit') as HTMLElement | null;
          const metaEl = root.querySelector('#meta-daily-visit') as HTMLElement | null;

          // formateador simple (puedes cambiar a '0' en vez de '–' si prefieres)
          const fmt = (n: number) => n === 0 ? '–' : n.toLocaleString();

          // asignar texto
          if (totalEl) totalEl.textContent = sumActual > 0 ? fmt(sumActual) : '–';
          if (metaEl) metaEl.textContent = hasMeta ? `Meta: ${fmt(sumForecast)}` : 'Meta: —';

          chDonut.setOption({
            tooltip: {
              trigger: 'item',
              // render en body para evitar que se corte por overflow del card
              appendToBody: true,
              confine: true,
              extraCssText: 'z-index: 99999; box-shadow: 0 8px 18px rgba(0,0,0,0.12);',
              // posicionar en el centro del contenedor del donut (elDonutDailyVisit está en scope)
              position: (_pos: any, _params: any, _dom: any, _rect: any) => {
                try {
                  if (!elDonutDailyVisit) return 'inside';
                  const r = elDonutDailyVisit.getBoundingClientRect();
                  const x = Math.round(r.left + r.width / 2);
                  const y = Math.round(r.top + r.height / 2) - 12; // ligeramente por encima del centro
                  return [x, y];
                } catch (e) {
                  return 'inside';
                }
              },
              // formatter robusto: acepta p como array u objeto y usa variables del scope
              formatter: (p: any) => {
                const item = Array.isArray(p) ? (p[0] ?? {}) : (p ?? {});
                if (!hasMeta) return 'Meta no definida';
                const name = item.name ?? '';
                if (name === 'Cumplido') {
                  return `${item.marker ?? ''} ${item.seriesName ?? 'Cumplimiento'}: ${sumActual} / ${sumForecast} (${percentage}%)`;
                }
                if (name === 'Pendiente') {
                  return `${item.marker ?? ''} Pendiente: ${Math.max(0, sumForecast - sumActual)}`;
                }
                // fallback genérico
                const value = item.value ?? 0;
                return `${item.marker ?? ''} ${item.seriesName ?? ''}: ${value}`;
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