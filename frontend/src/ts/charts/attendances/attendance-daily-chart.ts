// Attendance daily chart module (uses fetchWithTimeout from ../utils/api)
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

// Prefer to import a mkChart helper that encapsula echarts.init
// Ensure ../lib/echarts-setup exports `mkChart` (recommended) and optionally `echarts`.
import { mkChart as defaultMkChart } from '../../lib/echarts-setup';

export type AttendanceChartOptions = {
  days?: number;
  tz?: string;
  fetchWithTimeout?: typeof fetchWithTimeout;
  // mkChart should be a function (el: HTMLElement | null) => any
  mkChart?: (el: HTMLElement | null) => any;
  root?: Document | HTMLElement;
  apiBase?: string;
};

export function initAttendanceDailyChart(containerSelector = '#chart-attendance', opts: AttendanceChartOptions = {}) {
  const root = (opts.root ?? document) as Document;
  const el = root.querySelector(containerSelector) as HTMLDivElement | null;

  // Resolve mkChart function: prefer injected, then default import
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
      const urlActual = `${apiBase}/api/attendances/series?${params.toString()}`;
      const urlForecast = `${apiBase}/api/forecasts/attendance-forecast-series?${params.toString()}`;

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
        legend: { data: ['Asistencias', 'Forecast'], top: 6 },
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
            name: 'Asistencias',
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

      const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);
      if (!anyPositive) safeSetNoData(ch, el, 'Sin asistencias');

    } catch (err: any) {
      console.error('Attendance chart error', err);
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