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
  const s = String(v).replace(/\s+/g, '').replace(/,/g, '');
  const n = Number(s);
  return Number.isNaN(n) ? 0 : n;
}


async function fetchVisitsMap(dateArg?: string, tz?: string): Promise<Map<string, VisitsCell>> {
  const zone = tz ?? (typeof Intl !== 'undefined' ? Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC' : 'UTC');
  const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(zone)}` : `?tz=${encodeURIComponent(zone)}`;
  const url = `/api/site-supervision-visits/hourly-aggregated${q}`;

  try {
    const res = await fetchWithAuth(url);
    let payload: any = [];
    if (res.ok) {
      payload = await res.json().catch(() => []);
    } else {
      console.warn('[Site-Visit-Chart] fetch failed', res.status, res.statusText, url);
      return new Map();
    }

    // payload puede ser array directo o { data: [...] }
    const arr = Array.isArray(payload)
      ? payload
      : (payload && Array.isArray(payload.data) ? payload.data
         : (payload && Array.isArray(payload.result) ? payload.result : []));

    console.debug('[Site-Visit-Chart] fetch payload sample:', arr.slice(0,6));

    const m = new Map<string, VisitsCell>();
    (arr || []).forEach((it: any) => {
      const hh = extractHour(it);
      const count = toNumber(it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits ?? it?.countValue);
      const rawForecast = it?.forecast ?? it?.f ?? it?.visitsForecast;
      const forecast = rawForecast === undefined || rawForecast === null ? undefined : toNumber(rawForecast);
      m.set(hh, { count, forecast });
    });

    console.debug('[Site-Visit-Chart] normalized map sample:', Array.from(m.entries()).slice(0,6));
    return m;
  } catch (e) {
    console.warn('[Site-Visit-Chart] fetchVisitsMap error', e, url);
    return new Map();
  }
}


/** helper: small in-module "no data" painter (similar to client setNoData) */
function setNoData(chart: echarts.ECharts, msg = 'Sin visitas') {
  try {
    chart.setOption({
      graphic: {
        elements: [
          {
            type: 'text',
            left: 'center',
            top: 'middle',
            style: { text: msg, fill: '#9ca3af', fontSize: 14 }
          }
        ]
      }
    });
  } catch (e) {
    // ignore
  }
}

/** format hour label for tooltip/display: "00" -> "00:00" */
function hourDisplay(h: string): string {
  if (!h) return '';
  const hh = String(h).padStart(2, '0').slice(-2);
  return `${hh}:00`;
}

const COMMON_GRID = { left: '4%', right: '4%', top: 56, bottom: 36, containLabel: true };

function buildOption(labels: string[], valuesActual: number[], valuesForecast: (number | null)[]) {
  const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);

  return {
    legend: { data: ['Visitas', 'Forecast'], top: 8, left: 'center' }, // <- ensure center
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        if (!Array.isArray(params) || params.length === 0) return '';
        const first = params[0];
        let rawHour = first.axisValue ?? (first && typeof first.dataIndex === 'number' ? labels[first.dataIndex] : undefined);
        if (!rawHour && first.axisValueLabel) rawHour = first.axisValueLabel;
        const display = hourDisplay(String(rawHour ?? ''));
        const lines = params.map((p: any) => `${p.marker} ${p.seriesName}: ${p.value ?? 0}`);
        return `<b>${display}</b><br/>${lines.join('<br/>')}`;
      }
    },
    grid: COMMON_GRID, // <-- use the shared grid
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
 * Inicializa y devuelve control del chart de Site Visits.
 * - container: HTMLElement donde montar el chart
 * - opts: { tz?, theme?, showForecast?: boolean }
 *
 * Devuelve: { chart, refresh(date?), refreshFromMap(map), stop() }
 */
export async function initSiteVisitChart(container: HTMLElement, opts?:
                                { tz?: string; theme?: string | object; showForecast?: boolean }) {
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
    const valuesActual = labels.map(h => m.get(h)?.count ?? 0);
    const valuesForecast = labels.map(h => {
      const f = m.get(h)?.forecast;
      return f === undefined ? null : f;
    });

    console.debug('[Site-Visit-Chart] map keys:', Array.from(m.keys()));
    console.debug('[Site-Visit-Chart] sample entries:', Array.from(m.entries()).slice(0,6));
    console.debug('[Site-Visit-Chart] labels:', labels);
    console.debug('[Site-Visit-Chart] valuesActual:', valuesActual);
    console.debug('[Site-Visit-Chart] valuesForecast:', valuesForecast);

    const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);

    const option = buildOption(labels, valuesActual, opts?.showForecast ? valuesForecast : Array(24).fill(null));
    chart.setOption(option, { notMerge: false });

    if (!anyPositive) {
      setNoData(chart, 'Sin visitas');
    }
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
  chart.setOption(buildOption(hours24, Array(24).fill(0), Array(24).fill(null)));

  return {
    chart,
    refresh,         // call refresh(dateString) to fetch & redraw
    refreshFromMap,  // optionally call with normalized Map directly
    stop
  };
}