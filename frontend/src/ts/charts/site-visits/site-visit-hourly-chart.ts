/* site-visit-hourly-chart.ts */
import { fetchWithTimeout } from '../../utils/api';
import { safeSetNoData } from '../../utils/chart-uiutils';
import { mkChart as defaultMkChart } from '../../lib/echarts-setup';

export type VisitsHourlyOptions = {
  days?: number;
  tz?: string;
  mkChart?: (el: HTMLElement | null) => any;
  fetchWithTimeout?: typeof fetchWithTimeout;
  showForecast?: boolean;
  root?: Document | HTMLElement;
  apiBase?: string;
  metric?: string;
  siteId?: number | null;
  projectId?: number | null;
  forecastPath?: string | null;
};

type VisitsCell = { count: number; forecast?: number };
const hours24 = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));

function extractHour(item: any): string {
  const raw = item?.hour ?? item?.x ?? item?.label ?? item?.h ?? item?.time ?? '';
  const hh = String(raw ?? '').padStart(2, '0').slice(-2);
  return /^[0-2]\d$/.test(hh) ? hh : '00';
}

function toNumber(v: any): number {
  if (v === undefined || v === null || v === '') return 0;
  const s = String(v).replace(/\s+/g, '').replace(/,/g, '');
  const n = Number(s);
  return Number.isNaN(n) ? 0 : n;
}

const COMMON_GRID = { left: '4%', right: '4%', top: 56, bottom: 36, containLabel: true };

function buildOption(labels: string[], valuesActual: number[], valuesForecast: (number | null)[], anyPositiveOverride?: boolean) {
  const anyPositive = typeof anyPositiveOverride === 'boolean'
    ? anyPositiveOverride
    : (valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0));

  return {
    legend: { data: ['Visitas', 'Forecast'], top: 8, left: 'center' },
    tooltip: {
      trigger: 'axis',
      appendToBody: true,
      axisPointer: { type: 'line', snap: false },
      extraCssText: 'z-index: 99999; box-shadow: 0 4px 12px rgba(0,0,0,0.12);',
      formatter: (params: any) => {
        if (!Array.isArray(params) || params.length === 0) return '';
        const first = params[0];
        let idx = typeof first.dataIndex === 'number' ? first.dataIndex : undefined;
        const hourLabel = (idx !== undefined && labels && labels[idx]) ? labels[idx] : (first.axisValue ?? first.axisValueLabel ?? '');
        const hh = String(hourLabel ?? '').padStart(2, '0').slice(-2);
        const header = `<b>${hh}:00</b>`;
        const lines = params.map((p: any) => {
          const value = (p?.value === null || p?.value === undefined) ? '—' : p.value;
          return `${p.marker ?? ''} ${p.seriesName ?? ''}: ${value}`;
        });
        return `${header}<br/>${lines.join('<br/>')}`;
      }
    },
    grid: COMMON_GRID,
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLabel: { rotate: 0 }
    },
    yAxis: { type: 'value', min: 0 },
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
        showSymbol: true,
        symbolSize: 6,
        lineStyle: { width: 2, type: 'dashed' }
      }
    ],
    graphic: anyPositive ? { elements: [] } : undefined
  };
}

/**
 * initVisitHourlyChart(selector|element, opts)
 * Exports: { render, destroy, chart, container }
 */
export function initVisitHourlyChart(
  containerSelector: string | HTMLElement | null = '#chart-hourly-visit',
  opts: VisitsHourlyOptions = {}
) {
  const root = (opts.root ?? document) as Document;
  const el = (typeof containerSelector === 'string') ? root.querySelector(containerSelector) as HTMLDivElement | null : (containerSelector as HTMLElement | null);

  const mkChartFn = opts.mkChart ?? defaultMkChart;
  const fetchFn = opts.fetchWithTimeout ?? fetchWithTimeout;
  const apiBase = (opts.apiBase ?? '').replace(/\/+$/, '');
  const ch = mkChartFn ? mkChartFn(el) : null;

  // keep single donut instance so we can dispose it later
  let chDonut: any = null;

  async function render() {
    try {
      if (!ch || !el) return;

      const tz = opts.tz ?? Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC';
      const todayIso = new Intl.DateTimeFormat('en-CA', { timeZone: tz }).format(new Date());

      // default: show forecast unless explicitly false
      const showForecast = opts.showForecast ?? true;

      // hourly URL (if days===1 include date)
      const urlHourly = (opts.days === 1)
        ? `${apiBase}/api/site-supervision-visits/hourly-aggregated?date=${encodeURIComponent(todayIso)}&tz=${encodeURIComponent(tz)}`
        : `${apiBase}/api/site-supervision-visits/hourly-aggregated?tz=${encodeURIComponent(tz)}`;

      // forecast daily URL: mirror daily-chart signature (metric + optional site/project + forecastPath)
      const forecastMetric = opts.metric ?? 'VISITS';
      const paramsForecast = new URLSearchParams({ days: '1', tz, metric: forecastMetric });
      if (opts.siteId !== undefined && opts.siteId !== null) paramsForecast.set('siteId', String(opts.siteId));
      if (opts.projectId !== undefined && opts.projectId !== null) paramsForecast.set('projectId', String(opts.projectId));
      const forecastBase = opts.forecastPath ?? `${apiBase}/api/forecasts/forecast-series`;
      const urlForecastDaily = `${forecastBase}?${paramsForecast.toString()}`;

      console.debug('[Site-Visit-Hourly] urlHourly:', urlHourly);
      console.debug('[Site-Visit-Hourly] urlForecastDaily:', urlForecastDaily);

      const [resHourly, resForecast] = await Promise.all([
        fetchFn ? fetchFn(urlHourly, {}, 15000, true).catch((e: any) => { if (e?.name === 'AbortError') throw e; console.warn('[Site-Visit-Hourly] hourly fetch failed', e); return null; }) : fetch(urlHourly).catch(() => null),
        fetchFn ? fetchFn(urlForecastDaily, {}, 15000, true).catch((e: any) => { if (e?.name === 'AbortError') throw e; console.warn('[Site-Visit-Hourly] forecast fetch failed', e); return null; }) : fetch(urlForecastDaily).catch(() => null)
      ]);

      const parseOrEmpty = async (r: Response | null) => {
        if (!r) return [];
        if (!r.ok) return [];
        return await r.json().catch(() => []);
      };

      const [payloadHourly, payloadForecast] = await Promise.all([parseOrEmpty(resHourly), parseOrEmpty(resForecast)]);
      console.debug('[Site-Visit-Hourly] payloadHourly sample:', Array.isArray(payloadHourly) ? payloadHourly.slice(0,6) : payloadHourly);
      console.debug('[Site-Visit-Hourly] payloadForecast sample:', Array.isArray(payloadForecast) ? payloadForecast.slice(0,6) : payloadForecast);

      const arrHourly = Array.isArray(payloadHourly)
        ? payloadHourly
        : (payloadHourly && Array.isArray((payloadHourly as any).data) ? (payloadHourly as any).data : (payloadHourly && Array.isArray((payloadHourly as any).result) ? (payloadHourly as any).result : []));

      const arrForecast = Array.isArray(payloadForecast)
        ? payloadForecast
        : (payloadForecast && Array.isArray((payloadForecast as any).data) ? (payloadForecast as any).data : []);

      // Normalize hourly into map by hh
      const map = new Map<string, VisitsCell>();
      (arrHourly || []).forEach((it: any) => {
        const hh = extractHour(it);
        const count = toNumber(it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits ?? it?.countValue);
        const rawForecast = it?.forecast ?? it?.f ?? it?.visitsForecast;
        const forecast = rawForecast === undefined || rawForecast === null ? undefined : toNumber(rawForecast);
        map.set(hh, { count, forecast });
      });

      // Build forecastMap from arrForecast
      const forecastMap = new Map<string, number>();

      // detect if arrForecast contains hourly points OR daily points
      let looksHourlyForecast = false;
      let looksDailyForecast = false;

      if (Array.isArray(arrForecast) && arrForecast.length > 0) {
        // simple heuristics:
        // - hourly: elements are arrays [hh, value] OR objects with hour property numeric or 'HH'
        // - daily: elements have date/x like '2025-12-19' or objects without hour but with x/date
        for (const d of arrForecast) {
          if (Array.isArray(d) && d.length > 0 && /^[0-2]\d$/.test(String(d[0]).padStart(2, '0').slice(-2))) {
            looksHourlyForecast = true;
            break;
          }
          if (!Array.isArray(d) && (d?.hour !== undefined || d?.h !== undefined || d?.label !== undefined)) {
            looksHourlyForecast = true;
            break;
          }
          // consider daily if it has a date-like first element or x/date with '-'
          if (Array.isArray(d) && typeof d[0] === 'string' && d[0].includes('-')) {
            looksDailyForecast = true;
            // keep scanning to see if any hourly also present
          }
          if (!Array.isArray(d) && (typeof d?.x === 'string' && d.x.includes('-') || typeof d?.date === 'string' && d.date.includes('-'))) {
            looksDailyForecast = true;
          }
        }
      }

      if (looksHourlyForecast) {
        // existing logic: read hourly points into forecastMap
        arrForecast.forEach((d: any) => {
          let hh = '00';
          let val = 0;
          if (Array.isArray(d)) {
            hh = String(d[0] ?? '').padStart(2, '0').slice(-2);
            val = Number(d.length > 1 ? d[1] : 0) || 0;
          } else {
            hh = String(d?.hour ?? d?.x ?? d?.label ?? '').padStart(2, '0').slice(-2);
            val = typeof d?.value === 'number' ? d.value : Number(String(d?.y ?? d?.value ?? d?.forecast ?? d?.count ?? 0).replace(/,/g, '')) || 0;
          }
          if (/^[0-2]\d$/.test(hh)) forecastMap.set(hh, val);
        });
      } else if (looksDailyForecast) {
        // sum daily points for the target date(s) and distribute evenly across 24 hours
        // detect the day we care about (todayIso if opts.days === 1, else sum all returned)
        const targetDay = (opts.days === 1) ? todayIso : null;
        let dailyTotal = 0;
        for (const d of arrForecast) {
          let rawX: any; let rawY: any;
          if (Array.isArray(d)) {
            rawX = d[0]; rawY = d.length > 1 ? d[1] : 0;
          } else {
            rawX = d?.x ?? d?.date ?? d?.day ?? '';
            rawY = d?.y ?? d?.value ?? d?.forecast ?? d?.count ?? 0;
          }
          const x = String(rawX ?? '');
          const yNum = typeof rawY === 'number' ? rawY : Number(String(rawY ?? 0).replace(/,/g, '')) || 0;
          if (targetDay) {
            if (x.split('T')[0] === targetDay) dailyTotal += yNum;
          } else {
            dailyTotal += yNum;
          }
        }
        const avgPerHour = dailyTotal > 0 ? (dailyTotal / 24) : 0;
        // fill forecastMap with average per hour (use decimals but round to 2 dp)
        const avgRounded = Math.round(avgPerHour * 100) / 100;
        for (const hh of hours24) {
          forecastMap.set(hh, avgRounded);
        }
        console.debug('[Site-Visit-Hourly] distributed daily forecast total=', dailyTotal, 'avgPerHour=', avgRounded);
      } else {
        // no forecast data; leave forecastMap empty
      }

      const labels = hours24.slice();
      const valuesActual = labels.map(h => map.get(h)?.count ?? 0);
      const valuesForecast = labels.map(h => {
        if (forecastMap.has(h)) return forecastMap.get(h) ?? null;
        const f = map.get(h)?.forecast;
        return f === undefined ? null : f;
      });

      // Draw main hourly chart
      const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);

      if (!anyPositive) {
        // Show forecast even if actuals are zero (we consider valuesForecast)
        const hasForecastSeries = valuesForecast.some(v => Number(v) > 0);
        if (hasForecastSeries) {
          // show forecast line while actuals are zeros
          const forecastToUse = showForecast ? valuesForecast : Array(24).fill(null);
          // ensure yMax so small averages are visible
          const maxForecast = Math.max(0, ...valuesForecast.map(v => Number(v ?? 0)));
          const yMax = Math.max(1, maxForecast);
          ch.clear();
          ch.setOption(buildOption(labels, valuesActual, forecastToUse));
          try { ch.resize && ch.resize(); } catch (_) {}
        } else {
          safeSetNoData(ch, el, 'Sin visitas');
        }
      } else {
        const forecastToUse = showForecast ? valuesForecast : Array(24).fill(null);
        // determine yMax so peaks are visible (at least 1)
        const maxActual = Math.max(0, ...valuesActual.map(v => Number(v) || 0));
        const maxForecast = Math.max(0, ...valuesForecast.map(v => Number(v ?? 0)));
        const yMax = Math.max(1, maxActual, maxForecast);
        console.debug('[Site-Visit-Hourly] valuesForecast:', valuesForecast, 'yMax:', yMax);
        ch.clear();
        ch.setOption(buildOption(labels, valuesActual, forecastToUse));
        try { ch.resize && ch.resize(); } catch (_) {}
      }

      // Donut + meta: compute sums
      let sumActualDay = 0;
      let sumForecastDay = 0;

      // sum actual from hourly payload (robust)
      sumActualDay = (arrHourly || []).reduce((s: number, it: any) => {
        const raw = it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits ?? 0;
        const n = Number(String(raw ?? 0).replace(/,/g, '')) || 0;
        return s + n;
      }, 0);

      // try to compute sumForecastDay from forecast-series response (matching todayIso)
      if (Array.isArray(arrForecast) && arrForecast.length > 0) {
        for (const d of arrForecast) {
          let rawX: any; let rawY: any;
          if (Array.isArray(d)) { rawX = d[0]; rawY = d.length > 1 ? d[1] : 0; }
          else { rawX = d?.x ?? d?.date ?? d?.day ?? ''; rawY = d?.y ?? d?.value ?? d?.forecast ?? d?.count ?? 0; }
          const x = String(rawX ?? '').split('T')[0];
          const yNum = typeof rawY === 'number' ? rawY : Number(String(rawY ?? 0).replace(/,/g, '')) || 0;
          if (opts.days === 1) {
            if (x === todayIso) sumForecastDay += Number.isFinite(Number(yNum)) ? Number(yNum) : 0;
          } else {
            sumForecastDay += Number.isFinite(Number(yNum)) ? Number(yNum) : 0;
          }
        }
      }

      // fallback: sum hourly forecasts (either from forecastMap or from arrHourly)
      if (sumForecastDay === 0) {
        const hourlyForecastSum = labels.reduce((s, h) => {
          const fromForecastMap = forecastMap.has(h) ? (forecastMap.get(h) ?? 0) : 0;
          const fromHourly = map.get(h)?.forecast ?? 0;
          return s + Math.max(fromForecastMap, fromHourly);
        }, 0);
        if (hourlyForecastSum > 0) {
          sumForecastDay = hourlyForecastSum;
          console.debug('[Site-Visit-Hourly] derived sumForecastDay from hourly forecasts:', sumForecastDay);
        }
      }

      console.debug('[Site-Visit-Hourly] sumActualDay:', sumActualDay, 'sumForecastDay:', sumForecastDay);

      // Update DOM totals
      const totalEl = root.querySelector('#total-hourly-visit') as HTMLElement | null;
      const metaEl = root.querySelector('#meta-hourly-visit') as HTMLElement | null;
      if (totalEl) totalEl.textContent = sumActualDay > 0 ? String(sumActualDay) : '–';
      if (metaEl) metaEl.textContent = sumForecastDay > 0 ? `Meta: ${sumForecastDay}` : 'Meta: —';

      // Draw donut in #donut-hourly-visit (create or reuse single instance)
      const elDonut = root.querySelector('#donut-hourly-visit') as HTMLDivElement | null;
      if (elDonut) {
        const cs = getComputedStyle(elDonut);
        if ((!cs.height || cs.height === '0px') && (!cs.width || cs.width === '0px')) {
          elDonut.style.minHeight = elDonut.style.minHeight || '84px';
        }

        if (!chDonut) {
          chDonut = mkChartFn ? mkChartFn(elDonut) : null;
          if (!chDonut && (window as any).echarts && typeof (window as any).echarts.init === 'function') {
            try { chDonut = (window as any).echarts.init(elDonut, undefined, { renderer: 'canvas' }); } catch (_) { /* ignore */ }
          }
          console.debug('[Site-Visit-Hourly] chDonut instance after init:', chDonut);
        }

        if (chDonut) {
          const hasMeta = sumForecastDay > 0;
          const percentage = hasMeta ? Math.round((sumActualDay / sumForecastDay) * 100) : 0;
          const pctForSeries = hasMeta ? Math.min(100, Math.max(0, percentage)) : 100;
          const seriesData = !hasMeta
            ? [{ value: 1, name: 'Sin meta', itemStyle: { color: '#E5E7EB' } }]
            : [
                { value: pctForSeries, name: 'Cumplido', itemStyle: { color: '#10B981' } },
                { value: 100 - pctForSeries, name: 'Pendiente', itemStyle: { color: '#E5E7EB' } }
              ];
          chDonut.clear && chDonut.clear();
          chDonut.setOption({
            tooltip: {
              trigger: 'item',
              appendToBody: true,
              confine: true,
              extraCssText: 'z-index: 99999; box-shadow: 0 8px 18px rgba(0,0,0,0.12);',
              position: (_pos: any, _params: any, _dom: any, _rect: any) => {
                try {
                  if (!elDonut) return 'inside';
                  const r = elDonut.getBoundingClientRect();
                  const x = Math.round(r.left + r.width / 2);
                  const y = Math.round(r.top + r.height / 2) - 12;
                  return [x, y];
                } catch (e) {
                  return 'inside';
                }
              },
              formatter: (p: any) => {
                const item = Array.isArray(p) ? (p[0] ?? {}) : (p ?? {});
                if (!hasMeta) return 'Meta no definida';
                const name = item.name ?? '';
                if (name === 'Cumplido') {
                  return `${item.marker ?? ''} ${item.seriesName ?? 'Cumplimiento'}: ${sumActualDay} / ${sumForecastDay} (${percentage}%)`;
                }
                if (name === 'Pendiente') {
                  return `${item.marker ?? ''} Pendiente: ${Math.max(0, sumForecastDay - sumActualDay)}`;
                }
                const value = item.value ?? 0;
                return `${item.marker ?? ''} ${item.seriesName ?? ''}: ${value}`;
              }
            },
            series: [{
              name: 'Cumplimiento Visitas (día)',
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
                color: '#485572'
              },
              labelLine: { show: false },
              data: seriesData
            }]
          });
          try { chDonut.resize && chDonut.resize(); } catch (_) {}
          const cs2 = getComputedStyle(elDonut);
          console.debug('[Site-Visit-Hourly] elDonut size:', cs2.width, cs2.height);
        }
      }

    } catch (err: any) {
      if (err?.name === 'AbortError') {
        console.debug('Visits hourly fetch aborted (timeout/navigation)', err);
      } else {
        console.error('Error rendering visits hourly chart', err);
      }
      safeSetNoData(ch, el, 'Error de datos');
    }
  }

  function destroy() {
    try { if (ch && typeof ch.dispose === 'function') ch.dispose(); } catch (_) {}
    try {
      if (el) {
        const ph = el.querySelector('.chart-placeholder');
        if (ph) ph.remove();
      }
    } catch (_) {}
    try { if (chDonut && typeof chDonut.dispose === 'function') chDonut.dispose(); } catch (_) {}
  }

  return { render, destroy, chart: ch, container: el };
}