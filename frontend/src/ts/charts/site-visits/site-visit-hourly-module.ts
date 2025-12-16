// Módulo: inicializa el chart horario de visitas y el donut diario
// Uso: const ctrl = await initVisitsHourlyModule(root, { initSiteVisitChart, tz, todayIso, charts });
// ctrl.stop() para limpiar intervalos/recursos.

import { fetchWithAuth } from '../../utils/api';
import { mkChart, echarts } from '../../lib/echarts-setup';
import { setNoData } from '../../utils/chart-uiutils';

// Importa la implementación del chart puro (component)
import { initVisitHourlyChart as defaultInitVisitHourlyChart } from '../site-visits/site-visit-hourly-chart';

export type VisitsHourlyModuleOptions = {
  initVisitHourlyChart?: typeof defaultInitVisitHourlyChart;
  mkChart?: typeof mkChart;
  echarts?: typeof echarts;
  fetchWithAuth?: typeof fetchWithAuth;
  setNoData?: typeof setNoData;
  tz?: string;
  todayIso?: string;
  // array donde registrar charts para resize/cleanup (opcional)
  charts?: any[];
};

export type VisitsHourlyModuleController = {
  visitsHourlyCtrl: any | null;
  stop: () => void;
};

/**
 * Inicializa el visits hourly controller y el donut diario.
 * - root: elemento raíz donde buscar selectores (#chart-visit-hourly, #donut-daily-visit, #total-daily-visit, #meta-daily-visit)
 * - options: permite inyectar dependencias (test / override)
 */
export async function initVisitsHourlyModule(root: HTMLElement, options: VisitsHourlyModuleOptions = {}): Promise<VisitsHourlyModuleController> {
  const initVisitHourlyChart = options.initVisitHourlyChart ?? defaultInitVisitHourlyChart;
  const mkChartFn = options.mkChart ?? mkChart;
  const echartsLib = options.echarts ?? echarts;
  const fetchFn = options.fetchWithAuth ?? fetchWithAuth;
  const setNoDataFn = options.setNoData ?? setNoData;
  const tz = (options.tz ?? Intl.DateTimeFormat().resolvedOptions().timeZone) || 'UTC';
  const todayIso = options.todayIso ?? new Intl.DateTimeFormat('en-CA', { timeZone: tz }).format(new Date());
  const charts = options.charts ?? [];

  const elVisitHourly = root.querySelector('#chart-hourly-visit') as HTMLDivElement | null;
  let visitsHourlyCtrl: any | null = null;
  let visitsHourlyInterval: number | null = null;

  if (!elVisitHourly) {
    return { visitsHourlyCtrl: null, stop: () => {/* nothing */} };
  }

  try {
    visitsHourlyCtrl = await initVisitHourlyChart(elVisitHourly, { tz, showForecast: true });
    // refresh initial
    await visitsHourlyCtrl?.refresh?.(todayIso);

    // helper: normalizar varias formas de fecha a 'YYYY-MM-DD' (zona local)
    function toLocalIsoDateString(raw: any): string {
      if (raw === null || raw === undefined || raw === '') return '';
      const s = String(raw).trim();
      if (!s) return '';
      if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
      const parsed = new Date(s);
      if (!Number.isNaN(parsed.getTime())) {
        return `${parsed.getFullYear()}-${String(parsed.getMonth() + 1).padStart(2, '0')}-${String(parsed.getDate()).padStart(2, '0')}`;
      }
      const part = s.split('T')[0];
      return part && /^\d{4}-\d{2}-\d{2}$/.test(part) ? part : '';
    }

    // calcula forecast diario y sumatoria actual (hourly aggregated)
    async function computeDailySums(): Promise<{ sumActualDay: number; sumForecastDay: number; }> {
      let sumForecastDay = 0;
      let sumActualDay = 0;

      // forecast diario
      try {
        const urlForecastDaily = `/api/forecasts/forecast-series?days=1&tz=${encodeURIComponent(tz)}`;
        const resF = await fetchFn(urlForecastDaily);
        const payloadF = resF && resF.ok ? await resF.json().catch(() => []) : [];
        const arrF = Array.isArray(payloadF) ? payloadF : (payloadF?.data && Array.isArray(payloadF.data) ? payloadF.data : []);
        for (const d of (arrF || [])) {
          let rawX: any; let rawY: any;
          if (Array.isArray(d)) { rawX = d[0]; rawY = d.length > 1 ? d[1] : 0; }
          else { rawX = d?.x ?? d?.date ?? d?.day ?? ''; rawY = d?.y ?? d?.value ?? d?.forecast ?? d?.count ?? 0; }
          const x = toLocalIsoDateString(rawX);
          const yNum = typeof rawY === 'number' ? rawY : Number(String(rawY ?? 0).replace(/,/g, '')) || 0;
          if (x === todayIso) sumForecastDay += Number.isFinite(Number(yNum)) ? Number(yNum) : 0;
        }
      } catch (e) {
        console.warn('No se pudo obtener forecast diario', e);
        sumForecastDay = 0;
      }

      // hourly aggregated actual
      try {
        const urlHourly = `/api/site-supervision-visits/hourly-aggregated?date=${encodeURIComponent(todayIso)}&tz=${encodeURIComponent(tz)}`;
        const resH = await fetchFn(urlHourly);
        const payloadH = resH && resH.ok ? await resH.json().catch(() => []) : [];
        const arrH = Array.isArray(payloadH) ? payloadH : (payloadH?.data && Array.isArray(payloadH.data) ? payloadH.data : []);
        for (const it of (arrH || [])) {
          const raw = it?.count ?? it?.y ?? it?.cnt ?? it?.value ?? it?.visits ?? 0;
          const n = Number(String(raw ?? 0).replace(/,/g, '')) || 0;
          sumActualDay += n;
        }
      } catch (e) {
        console.warn('No se pudo obtener hourly-aggregated para hoy', e);
        sumActualDay = 0;
      }

      return { sumActualDay, sumForecastDay };
    }

    // render donut + update DOM totals
    async function updateDailyDonutAndMeta() {
      try {
        const { sumActualDay, sumForecastDay } = await computeDailySums();

        const totalDailyEl = root.querySelector('#total-hourly-visit') as HTMLElement | null;
        const metaDailyEl = root.querySelector('#meta-hourly-visit') as HTMLElement | null;
        if (totalDailyEl) totalDailyEl.textContent = sumActualDay > 0 ? String(sumActualDay) : '–';
        if (metaDailyEl) metaDailyEl.textContent = sumForecastDay > 0 ? `Meta: ${sumForecastDay}` : 'Meta: —';

        const elDonutDaily = root.querySelector('#donut-hourly-visit') as HTMLDivElement | null;
        if (!elDonutDaily) return;

        const chDonut = mkChartFn(elDonutDaily) ?? (echartsLib && (echartsLib as any).init ? (echartsLib as any).init(elDonutDaily, undefined, { renderer: 'canvas' }) : null);
        if (!chDonut) return;
        if (!charts.includes(chDonut)) charts.push(chDonut);

        const hasMeta = sumForecastDay > 0;
        const percentage = hasMeta ? Math.round((sumActualDay / sumForecastDay) * 100) : 0;
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
              if (p && p.name === 'Cumplido') return `${p.marker} ${p.seriesName}: ${sumActualDay} / ${sumForecastDay} (${percentage}%)`;
              if (p && p.name === 'Pendiente') return `${p.marker} Pendiente: ${Math.max(0, sumForecastDay - sumActualDay)}`;
              return '';
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
              color: '#485572ff'
            },
            labelLine: { show: false },
            data: seriesData
          }]
        });

        // if hourly chart exists, add meta reference series (average per hour)
        if (visitsHourlyCtrl?.chart && sumForecastDay > 0) {
          try {
            const avgPerHour = sumForecastDay / 24;
            const metaName = 'Meta diaria (promedio/h)';
            const chart = visitsHourlyCtrl.chart;
            const currentOpt: any = (chart.getOption && typeof chart.getOption === 'function') ? chart.getOption() : {};
            const existingSeries: any[] = Array.isArray(currentOpt.series) ? currentOpt.series : [];
            const baseSeries = existingSeries.filter(s => !(s && s.name === metaName));
            const metaSeries = {
              name: metaName,
              type: 'line',
              data: Array(24).fill(Number((avgPerHour).toFixed(2))),
              lineStyle: { color: '#f59e0b', width: 1, type: 'dashed' },
              symbol: 'none',
              silent: true,
              tooltip: { show: false }
            };
            const newOpt = Object.assign({}, currentOpt, { series: baseSeries.concat([metaSeries]) });
            chart.setOption(newOpt, /* notMerge = */ true);
          } catch (e) {
            console.warn('No se pudo dibujar referencia de forecast en hourly chart', e);
          }
        }

      } catch (err) {
        console.warn('Error actualizando forecast/donut diario:', err);
      }
    }

    // initial update
    await updateDailyDonutAndMeta();

    // schedule periodic refresh of hourly chart and donut
    visitsHourlyInterval = window.setInterval(() => {
      visitsHourlyCtrl?.refresh?.(todayIso);
      // refresh donut/meta as well
      updateDailyDonutAndMeta().catch(() => {});
    }, 60_000);

    // bind click handler safely
    visitsHourlyCtrl?.chart?.on?.('click', (params: any) => {
      const hour = params?.name ?? params?.data?.hour;
      console.log('Drilldown visits hourly click', hour);
    });

    if (visitsHourlyCtrl?.chart && !charts.includes(visitsHourlyCtrl.chart)) charts.push(visitsHourlyCtrl.chart);

    // expose cleanup for caller
    const stop = () => {
      try { if (visitsHourlyInterval) { window.clearInterval(visitsHourlyInterval); visitsHourlyInterval = null; } } catch {}
      try { visitsHourlyCtrl?.stop?.(); } catch {}
    };

    return { visitsHourlyCtrl, stop };

  } catch (err) {
    console.error('Error inicializando visits hourly chart', err);
    const tmp = mkChart(elVisitHourly);
    setNoDataFn(tmp, 'Error de datos');
    return {
      visitsHourlyCtrl: null,
      stop: () => {}
    };
  }
}