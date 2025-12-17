import { fetchWithTimeout } from '../../utils/api';
import { mkChart as defaultMkChart } from '../../lib/echarts-setup';
import { safeSetNoData } from '../../utils/chart-uiutils';

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
  const anyPositive = typeof anyPositiveOverride === 'boolean' ? anyPositiveOverride : (valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0));

  return {
    legend: { data: ['Visitas', 'Forecast'], top: 8, left: 'center' },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        if (!Array.isArray(params) || params.length === 0) return '';
        const first = params[0];
        let rawHour = first.axisValue ?? (first && typeof first.dataIndex === 'number' ? labels[first.dataIndex] : undefined);
        if (!rawHour && first.axisValueLabel) rawHour = first.axisValueLabel;
        const hh = String(rawHour ?? '').padStart(2, '0').slice(-2);
        const display = `${hh}:00`;
        const lines = params.map((p: any) => `${p.marker} ${p.seriesName}: ${p.value ?? 0}`);
        return `<b>${display}</b><br/>${lines.join('<br/>')}`;
      }
    },
    grid: COMMON_GRID,
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLabel: { rotate: 0 }
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
    graphic: anyPositive ? { elements: [] } : undefined
  };
}

/**
 * initVisitsHourlyChart(selector|element, opts)
 * opts: { tz?, mkChart?, fetchWithTimeout?, showForecast?, root?, apiBase? }
 *
 * Exports: { render, destroy, chart, container }
 */
export function initVisitHourlyChart(
  containerSelector: string | HTMLElement | null = '#chart-hourly-visit',
  opts: {
    tz?: string;
    mkChart?: (el: HTMLElement | null) => any;
    fetchWithTimeout?: typeof fetchWithTimeout;
    showForecast?: boolean;
    root?: Document | HTMLElement;
    apiBase?: string;
  } = {}
) {
  const root = (opts.root ?? document) as Document;
  const container = (typeof containerSelector === 'string') ? root.querySelector(containerSelector) as HTMLDivElement | null : (containerSelector as HTMLElement | null);

  const mkChartFn = opts.mkChart ?? defaultMkChart;
  const fetchFn = opts.fetchWithTimeout ?? fetchWithTimeout;
  const apiBase = opts.apiBase ?? '';

  const ch = mkChartFn ? mkChartFn(container) : null;

  async function render() {
    try {
      if (!ch || !container) {
        // Nothing to draw into
        return;
      }

      const tz = opts.tz ?? Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC';
      // Use fetchFn(url, init?, timeoutMs?, useFetchWithAuth?) signature like fetchWithTimeout
      const url = `${apiBase}/api/site-supervision-visits/hourly-aggregated?tz=${encodeURIComponent(tz)}`;

      const res = await (fetchFn ? fetchFn(url, {}, 15000, true).catch((e: any) => { 
        if (e?.name === 'AbortError') throw e;
        console.warn('[Site-Visit-Hourly] fetch failed', e);
        return null;
      }) : fetch(url).catch(() => null));

      const parseOrEmpty = async (r: Response | null) => {
        if (!r) return [];
        if (!r.ok) return [];
        return await r.json().catch(() => []);
      };

      const payload = await parseOrEmpty(res);
      const arr = Array.isArray(payload) ? payload : (payload && Array.isArray((payload as any).data) ? (payload as any).data : (payload && Array.isArray((payload as any).result) ? (payload as any).result : []));

      // Normalize into 24-length arrays
      const map = new Map<string, VisitsCell>();
      (arr || []).forEach((it: any) => {
        const hh = extractHour(it);
        const count = toNumber(it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits ?? it?.countValue);
        const rawForecast = it?.forecast ?? it?.f ?? it?.visitsForecast;
        const forecast = rawForecast === undefined || rawForecast === null ? undefined : toNumber(rawForecast);
        map.set(hh, { count, forecast });
      });

      const labels = hours24.slice();
      const valuesActual = labels.map(h => map.get(h)?.count ?? 0);
      const valuesForecast = labels.map(h => {
        const f = map.get(h)?.forecast;
        return f === undefined ? null : f;
      });

      // If no data at all, show placeholder
      const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);
      if (!anyPositive) {
        safeSetNoData(ch, container, 'Sin visitas');
        return;
      }

      ch.clear();
      ch.setOption(buildOption(labels, valuesActual, opts.showForecast ? valuesForecast : Array(24).fill(null)));

    } catch (err: any) {
      if (err?.name === 'AbortError') {
        console.debug('Visits hourly fetch aborted (timeout/navigation)', err);
      } else {
        console.error('Error rendering visits hourly chart', err);
      }
      // show placeholder on error
      if (ch && container) safeSetNoData(ch, container, 'Error de datos');
    }
  }

  function destroy() {
    try { if (ch && typeof ch.dispose === 'function') ch.dispose(); } catch (_) {}
    try {
      if (container) {
        const ph = container.querySelector('.chart-placeholder');
        if (ph) ph.remove();
      }
    } catch (_) {}
  }

  return { render, destroy, chart: ch, container };
}