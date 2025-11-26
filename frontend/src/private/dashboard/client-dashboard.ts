import { echarts } from '../../lib/echarts-setup';
import { initSiteVisitChart } from './site-visits/site-visit-hourly-chart';
import { fetchWithAuth } from './api/api';

type Point = { x: string; y: number };
type SiteVisitCountDto = { siteId: number; siteName: string; count: number };

function firstDayISO(d = new Date()) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
}
function lastDayISO(d = new Date()) {
  const y = d.getFullYear(); const m = d.getMonth() + 1;
  const last = new Date(y, m, 0).getDate();
  return `${y}-${String(m).padStart(2, '0')}-${String(last).padStart(2, '0')}`;
}

export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#client-dashboard-root') as HTMLElement) || container;
  if (!root || root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';

  const clientId = root.getAttribute('data-client-id') || '1';
  const from = firstDayISO();
  const to   = lastDayISO();


  // DOM: asegúrate de que existan estos contenedores en tu fragmento
  const elAsis   = root.querySelector('#chart-att') as HTMLDivElement;
  const elPatrol = root.querySelector('#chart-patrol') as HTMLDivElement;
  const elInc    = root.querySelector('#chart-inc') as HTMLDivElement;
  const elVisit  = root.querySelector('#chart-visit') as HTMLDivElement;
  const elVisitSite = root.querySelector('#chart-visit-site') as HTMLDivElement;


  // -------- Chart helpers --------
  const charts: echarts.ECharts[] = [];
  const mkChart = (el?: HTMLDivElement) => el ? echarts.init(el, undefined, { renderer: 'canvas' }) : null;
  const setNoData = (chart: echarts.ECharts | null, msg = 'Sin datos') => {
    if (!chart) return;
    chart.setOption({ graphic: { elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: msg, fill: '#9ca3af', fontSize: 14 } }] } });
  };


  // ---------------- helper: fetch with timeout ----------------
  async function fetchWithTimeout(
    input: RequestInfo | URL,
    init: RequestInit = {},
    timeoutMs = 15000,
    useFetchWithAuth = false
  ): Promise<Response> {
    const controller = new AbortController();
    const mergedInit: RequestInit = { ...init, signal: controller.signal };
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);


    try {
      if (useFetchWithAuth) {
        // fetchWithAuth acepta RequestInit y reenvía signal/headers porque hace: const opts = { ..., ...init, headers }
        return await fetchWithAuth(String(input), mergedInit);
      }
      return await fetch(input, mergedInit);
    } finally {
      clearTimeout(timeoutId);
      // No abort aquí; abort already triggered by timeout if needed or the fetch finished.
    }
  }


// --------------- Chart visit hourly ------------------//
const elVisitHourly = root.querySelector('#chart-visit-hourly') as HTMLDivElement | null;

type SiteVisitChartController = Awaited<ReturnType<typeof initSiteVisitChart>>;
let visitsHourlyCtrl: SiteVisitChartController | null = null; // controlador del chart horario

if (elVisitHourly) {
  try {
    visitsHourlyCtrl = await initSiteVisitChart(elVisitHourly, {
      tz: Intl.DateTimeFormat().resolvedOptions().timeZone,
      showForecast: true
    });

    const todayIso = new Date().toISOString().slice(0, 10);
    await visitsHourlyCtrl.refresh(todayIso);

    const visitsHourlyInterval = window.setInterval(() => {
      visitsHourlyCtrl?.refresh(todayIso);
    }, 60_000);

    // protege el acceso al chart con ?. por si algo sale mal
    visitsHourlyCtrl?.chart?.on('click', (params: any) => {
      const hour = params?.name ?? params?.data?.hour;
      console.log('Drilldown visits hourly click', hour);
    });

    if (visitsHourlyCtrl?.chart) charts.push(visitsHourlyCtrl.chart);

    (root as any).__visitsHourlyCleanup = {
      stop: () => {
        try { window.clearInterval(visitsHourlyInterval); } catch {}
        try { visitsHourlyCtrl?.stop(); } catch {}
      }
    };
  } catch (err) {
    console.error('Error inicializando visits hourly chart', err);
    const tmp = mkChart(elVisitHourly);
    setNoData(tmp, 'Error de datos');
  }
}





  // -------- Asistencia (línea con área) --------
  const chAsis = mkChart(elAsis); if (chAsis) charts.push(chAsis);
  try {
    const res = await fetchWithAuth(`/api/attendance/series?clientId=${clientId}&from=${from}&to=${to}&action=IN`);
    if (res.ok) {
      const data: Point[] = await res.json().catch(() => []);
      const labels = data.map(d => d.x);
      const values = data.map(d => d.y);
      chAsis?.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 24, bottom: 32 },
        xAxis: { type: 'category', boundaryGap: false, data: labels },
        yAxis: { type: 'value' },
        series: [{ type: 'line', smooth: true, areaStyle: {}, data: values }],
        graphic: values.some(v => Number(v) > 0) ? { elements: [] } : undefined
      });
      if (!values.some(v => Number(v) > 0)) setNoData(chAsis);
    } else setNoData(chAsis, 'Error de datos');
  } catch { setNoData(chAsis, 'Error de datos'); }




// -------- Rondas por día (línea con área) --------
const chPatrol = mkChart(elPatrol); if (chPatrol) charts.push(chPatrol);
try {
  // opcional: enviar tz para que "hoy" y los rangos coincidan con la zona del usuario
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
  const url = `/api/site-patrols/series?clientId=${clientId}&from=${from}&to=${to}&tz=${encodeURIComponent(tz)}`;

  const res = await fetchWithAuth(url);
  if (res.ok) {
    const data: Point[] = await res.json().catch(() => []);
    const labels = data.map(d => d.x);
    const values = data.map(d => d.y);

    chPatrol?.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 24, bottom: 32 },
      xAxis: { type: 'category', boundaryGap: false, data: labels },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: values,
        color: '#34D399' // color para rondas (verde), cámbialo si quieres
      }],
      graphic: values.some(v => Number(v) > 0) ? { elements: [] } : undefined
    });

    if (!values.some(v => Number(v) > 0)) setNoData(chPatrol);
  } else {
    setNoData(chPatrol, 'Error de datos');
  }
} catch (err) {
  console.error('Error cargando series de rondas', err);
  setNoData(chPatrol, 'Error de datos');
}

// Opcional: asegurar resize cuando cambie el contenedor
window.addEventListener('resize', () => chPatrol?.resize());




// ---------------- Visitas diarias: Reales vs Forecast (últimos 7 días) ----------------
const chVisit = mkChart(elVisit);
if (chVisit) charts.push(chVisit);

// helpers: formatea 'YYYY-MM-DD' -> '12-ene' (sin punto)
// MUST be declared before setOption to avoid TDZ errors
const locale = navigator.language || 'es';
const monthFormatter = new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'short' });

function toDateFromIsoDay(isoDay: string): Date | null {
  if (!isoDay) return null;
  // si vienen con hora, Date acepta ISO; si vienen solo 'YYYY-MM-DD' añadimos T00:00:00
  const raw = isoDay.includes('T') ? isoDay : `${isoDay}T00:00:00`;
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
}

function shortLabelFromIso(isoDay: string): string {
  const d = toDateFromIsoDay(isoDay);
  if (!d) return isoDay ?? '';
  const txt = monthFormatter.format(d).replace('.', '');
  return txt.replace(/\s+/, '-'); // "12 ene" -> "12-ene"
}

// helper: build last N ISO dates in local timezone (YYYY-MM-DD)
function buildLastNDatesIso(days: number, zoneTz?: string): string[] {
  const out: string[] = [];
  const now = new Date();
  // use local timezone of browser; zoneTz is only used server-side for request
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(now.getDate() - i);
    out.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`);
  }
  return out;
}

try {
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
  const days = 7;

  const urlActual = `/api/site-supervision-visits/series?days=${days}&tz=${encodeURIComponent(tz)}`;
  const urlForecast = `/api/forecasts/forecast-series?days=${days}&tz=${encodeURIComponent(tz)}`;

  // pedir ambas en paralelo con timeout y auth
  const [resActual, resForecast] = await Promise.all([
    fetchWithTimeout(urlActual, {}, 15000, true).catch(err => {
      console.warn('Fetch actual visits failed', err);
      return null;
    }),
    fetchWithTimeout(urlForecast, {}, 15000, true).catch(err => {
      console.warn('Fetch forecast visits failed', err);
      return null;
    })
  ]);

  // manejar errores de respuesta
  const parseOrEmpty = async (r: Response | null, name: string) => {
    if (!r) return [];
    if (!r.ok) {
      console.warn(`${name} returned status`, r.status);
      return [];
    }
    return await r.json().catch((e: any) => {
      console.warn(`${name} json parse error`, e);
      return [];
    });
  };

  const [dataActual, dataForecast] = await Promise.all([
    parseOrEmpty(resActual, 'actual visits'),
    parseOrEmpty(resForecast, 'forecast visits')
  ]);

  // Si ambos vacíos -> mostrar sin datos
  const bothEmpty = (!Array.isArray(dataActual) || dataActual.length === 0)
                 && (!Array.isArray(dataForecast) || dataForecast.length === 0);
  if (bothEmpty) {
    setNoData(chVisit, 'Sin datos');
    return;
  }




// Normaliza una fecha ISO/fecha-only a 'YYYY-MM-DD' en la zona local del navegador
function isoToLocalIsoDateString(iso: unknown): string {
  if (iso === null || iso === undefined) return '';

  const s = String(iso).trim();
  if (!s) return '';

  // Caso 1: fecha sola YYYY-MM-DD -> construir Date en zona local para evitar interpretarla como UTC
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    const [yStr, mStr, dStr] = s.split('-');
    const y = Number(yStr);
    const m = Number(mStr);
    const d = Number(dStr);
    if (Number.isFinite(y) && Number.isFinite(m) && Number.isFinite(d)) {
      const dt = new Date(y, m - 1, d); // creamos en zona local
      return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
    }
    return '';
  }

  // Caso 2: si viene con hora o zona, dejar que Date lo parse (y convertir a fecha local)
  const parsed = new Date(s);
  if (Number.isNaN(parsed.getTime())) {
    // Fallback: tomar la parte antes de 'T' si existe
    const part = s.split('T')[0];
    return part || '';
  }
  return `${parsed.getFullYear()}-${String(parsed.getMonth() + 1).padStart(2, '0')}-${String(parsed.getDate()).padStart(2, '0')}`;
}


type Point = { x: string; y: number };

// Normaliza entradas en varios formatos a { x: 'YYYY-MM-DD', y: number }
const norm = (arr: any[] | null | undefined): Point[] => {
  if (!Array.isArray(arr)) return [];

  return arr.map(d => {
    // rawX: puede venir en d.x o d.date o ser un array [date,value]
    let rawX: any = '';
    if (Array.isArray(d)) rawX = d[0];
    else rawX = d?.x ?? d?.date ?? d?.day ?? '';

    // rawY: puede venir en d.y o d.value o ser un array [date,value]
    let rawY: any = 0;
    if (Array.isArray(d) && d.length > 1) rawY = d[1];
    else rawY = d?.y ?? d?.value ?? 0;

    const x = isoToLocalIsoDateString(rawX);
    const yNum = (typeof rawY === 'number') ? rawY : Number(String(rawY ?? 0));
    return { x, y: Number.isFinite(yNum) ? yNum : 0 };
  })
  // eliminar entradas inválidas (sin fecha)
  .filter(p => p.x && typeof p.x === 'string' && p.x.length >= 4);
};


  const normActual = norm(dataActual);
  const normForecast = norm(dataForecast);

  // Construir conjunto de labels (union) y ordenar.
  // Preferencia: forzar exactamente los últimos `days` días para que "hoy" siempre aparezca.
  let labels = buildLastNDatesIso(days, tz); // ['2025-11-18', ... , '2025-11-24']


const mapFrom = (arr: { x: string; y: number }[]) => {
  const m = new Map<string, number>();
  arr.forEach(p => {
    if (!p || !p.x) return;
    const prev = m.get(p.x) ?? 0;
    m.set(p.x, prev + (Number.isFinite(Number(p.y)) ? Number(p.y) : 0));
  });
  return m;
};


  const mActual = mapFrom(normActual);
  const mForecast = mapFrom(normForecast);

  const valuesActual = labels.map(l => mActual.has(l) ? mActual.get(l)! : 0);
  const valuesForecast = labels.map(l => mForecast.has(l) ? mForecast.get(l)! : 0);


  // --- Donut específico para "Visitas" (weekly) ---
  const elDonutVisit = root.querySelector('#donut-visit') as HTMLDivElement | null;
  if (elDonutVisit) {
    const chDonutVisit = mkChart(elDonutVisit);
    if (chDonutVisit) {
      charts.push(chDonutVisit);

      const sumActualVisit = valuesActual.reduce((s, v) => s + (Number(v) || 0), 0);
      const sumForecastVisit = valuesForecast.reduce((s, v) => s + (Number(v) || 0), 0);

      const hasMeta = sumForecastVisit > 0;
      const percentage = hasMeta ? Math.round((sumActualVisit / sumForecastVisit) * 100) : 0;
      const pctForSeries = hasMeta ? Math.min(100, Math.max(0, percentage)) : 100;

      const seriesData = !hasMeta
        ? [{ value: 1, name: 'Sin meta', itemStyle: { color: '#E5E7EB' } }]
        : [
            { value: pctForSeries, name: 'Cumplido', itemStyle: { color: '#10B981' } },
            { value: 100 - pctForSeries, name: 'Pendiente', itemStyle: { color: '#E5E7EB' } }
          ];

      chDonutVisit.setOption({
        tooltip: {
          trigger: 'item',
          formatter: (p: any) => {
            if (!hasMeta) return 'Meta no definida';
            if (p && p.name === 'Cumplido') {
              return `${p.marker} ${p.seriesName}: ${sumActualVisit} / ${sumForecastVisit} (${percentage}%)`;
            }
            if (p && p.name === 'Pendiente') {
              return `${p.marker} Pendiente: ${Math.max(0, sumForecastVisit - sumActualVisit)}`;
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
            fontSize: 12,
            fontWeight: 700,
            color: '#111827'
          },
          labelLine: { show: false },
          data: seriesData
        }]
      });
    }
  }


  console.debug('[visits-chart] labels:', labels);
  console.debug('[visits-chart] normActual:', normActual);
  console.debug('[visits-chart] normForecast:', normForecast);
  console.debug('[visits-chart] valuesActual:', valuesActual);
  console.debug('[visits-chart] valuesForecast:', valuesForecast);


  if (!chVisit) {
    setNoData(chVisit, 'Sin datos');
    return;
  }

  // pintar chart
  chVisit.clear();
  chVisit.setOption({
    legend: { data: ['Visitas', 'Forecast'], top: 6 },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        if (!Array.isArray(params) || params.length === 0) return '';
        const first = params[0];
        // axisValue puede ser undefined; buscamos fallback por dataIndex en labels[]
        let rawDate = first.axisValue ?? (first && typeof first.dataIndex === 'number' ? labels[first.dataIndex] : undefined);
        if (!rawDate && first.axisValueLabel) rawDate = first.axisValueLabel;
        const displayDate = shortLabelFromIso(String(rawDate ?? ''));
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
        formatter: function (value: string) {
          return shortLabelFromIso(value);
        }
      }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Visitas reales',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: valuesActual,
        color: '#0ea5e9',
        showSymbol: false,
        lineStyle: { width: 2 }
      },
      {
        name: 'Forecast (previsto)',
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

  // Si ambas series son cero -> mostrar mensaje "Sin visitas"
  const anyPositive = valuesActual.some(v => Number(v) > 0) || valuesForecast.some(v => Number(v) > 0);
  if (!anyPositive) setNoData(chVisit, 'Sin visitas');

} catch (err: any) {
  if (err?.name === 'AbortError') {
    console.debug('Visits/Forecast fetch aborted (timeout or navigation)', err);
  } else {
    console.error('Error obteniendo series de visitas/forecast', err);
  }
  setNoData(chVisit, 'Error de datos');
}







  // -------- Visitas por sitio (barra horizontal) --------
  const chVisitSite = mkChart(elVisitSite); if (chVisitSite) charts.push(chVisitSite);
  try {
    const res = await fetchWithAuth(`/api/site-supervision-visits/series-by-site?clientId=${clientId}&from=${from}&to=${to}`);
    if (res.ok) {
      const data: SiteVisitCountDto[] = await res.json().catch(() => []);
      const labels = data.map(d => d.siteName);
      const values = data.map(d => d.count);
      chVisitSite?.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 60, right: 16, top: 24, bottom: 32 },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: labels, axisLabel: { interval: 0, fontSize: 12 } },
        series: [{ type: 'bar', data: values, itemStyle: { color: '#0ea5e9' } }],
        graphic: values.some(v => Number(v) > 0) ? { elements: [] } : undefined
      });
      if (!values.some(v => Number(v) > 0)) setNoData(chVisitSite);
    } else setNoData(chVisitSite, 'Error de datos');
  } catch { setNoData(chVisitSite, 'Error de datos'); }

  // Resize robusto (asegura que los 4 gráficos se ajusten)
  const ro = new ResizeObserver(() => charts.forEach(c => c?.resize()));
  [elAsis, elInc, elVisit, elVisitSite].forEach(el => el && ro.observe(el));

  // Limpieza al salir del fragmento
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    charts.forEach(c => { try { c.dispose(); } catch {} });
    root.dataset.echartsInit = '0';
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}

// Auto-init si insertas el script directamente en el fragmento
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    const root = document.getElementById('client-dashboard-root');
    if (root) init({ container: root });
  });
} else {
  const root = document.getElementById('client-dashboard-root');
  if (root) init({ container: root });
}