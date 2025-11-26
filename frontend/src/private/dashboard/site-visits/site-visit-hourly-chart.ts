import { echarts } from '../../../lib/echarts-setup';
import { fetchWithAuth } from '../api/api';

type VisitsCell = { count: number; forecast?: number };
const hours24 = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));

function extractHour(item: any): string {
  const raw = item?.hour ?? item?.x ?? item?.label ?? item?.h ?? item?.time ?? '';
  const hh = String(raw ?? '').padStart(2, '0').slice(-2);
  return /^[0-2]\d$/.test(hh) ? hh : '00';
}

function toNumber(v: any): number {
  if (v === undefined || v === null || v === '') return 0;
  const n = Number(v);
  return Number.isNaN(n) ? 0 : n;
}

async function fetchVisitsMap(dateArg?: string, tz?: string): Promise<Map<string, VisitsCell>> {
  const zone = tz ?? (typeof Intl !== 'undefined' ? Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC' : 'UTC');
  const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(zone)}` : `?tz=${encodeURIComponent(zone)}`;
  const url = `/api/site-supervision-visits/hourly-aggregated${q}`;

  try {
    const res = await fetchWithAuth(url);
    const arr = res.ok ? await res.json().catch(() => []) : [];
    const m = new Map<string, VisitsCell>();
    (arr || []).forEach((it: any) => {
      const hh = extractHour(it);
      const count = toNumber(it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits);
      const rawForecast = it?.forecast ?? it?.f ?? it?.visitsForecast;
      const forecast = rawForecast === undefined || rawForecast === null ? undefined : toNumber(rawForecast);
      m.set(hh, { count, forecast });
    });
    return m;
  } catch (e) {
    console.warn('[Site-Visit-Chart] fetchVisitsMap error', e);
    return new Map();
  }
}

function buildOption(labels: string[], values: number[], forecastValues?: (number | null)[]) {
  const series: any[] = [
    {
      name: 'Visitas',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: values
    }
  ];
  if (forecastValues && forecastValues.some(v => v !== null && v !== undefined)) {
    series.push({
      name: 'Visitas (forecast)',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: forecastValues,
      lineStyle: { type: 'dashed' },
      itemStyle: { opacity: 0.8 }
    });
  }

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '6%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: false,
      axisTick: { alignWithLabel: true },
      axisLabel: { interval: 0, showMinLabel: true, showMaxLabel: true }
    },
    yAxis: {
      type: 'value',
      min: 0
    },
    series
  };
}

/**
 * Inicializa y devuelve control del chart de Site Visits.
 * - container: HTMLElement donde montar el chart
 * - opts: { tz?, theme?, showForecast?: boolean }
 *
 * Devuelve: { chart, refresh(date?), stop() }
 */
export async function initSiteVisitChart(container: HTMLElement, opts?: { tz?: string; theme?: string | object; showForecast?: boolean }) {
  if (!container) throw new Error('container is required for initSiteVisitChart');

  const chart = echarts.init(container, opts?.theme as any);
  let destroyed = false;

  // resize handler
  const onResize = () => {
    try { chart.resize(); } catch (e) { /* ignore */ }
  };
  window.addEventListener('resize', onResize);

  async function refreshFromMap(m: Map<string, VisitsCell>) {
    if (destroyed) return;
    const labels = hours24;
    const values = labels.map(h => m.get(h)?.count ?? 0);
    const forecastValues = labels.map(h => {
      const f = m.get(h)?.forecast;
      return f === undefined ? null : f;
    });
    const option = buildOption(labels, values, opts?.showForecast ? forecastValues : undefined);
    chart.setOption(option, { notMerge: false });
  }

  async function refresh(dateArg?: string) {
    if (destroyed) return;
    const m = await fetchVisitsMap(dateArg, opts?.tz);
    await refreshFromMap(m);
  }

  function stop() {
    destroyed = true;
    try { window.removeEventListener('resize', onResize); } catch (_) {}
    try { chart.dispose(); } catch (_) {}
  }

  // initial blank render to reserve layout and show empty axes
  chart.setOption(buildOption(hours24, Array(24).fill(0)));

  return {
    chart,
    refresh,         // call refresh(dateString) to fetch & redraw
    refreshFromMap,  // optionally call with normalized Map directly
    stop
  };
}