import { echarts } from '../../lib/echarts-setup';
import { initThreeMetrics } from './init-three-metrics';
import { fetchWithAuth } from './api/api';

// llamar initThreeMetrics para los 3 gráficos por hora

type Point = { x: string; y: number };
type KPIs = { asistenciaHoy: number; rondasHoy: number; visitasHoy: number; incidentesAbiertos: number };
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


  // -------- KPIs (cards) --------
  try {
    // Obtener la zona horaria del navegador (fallback a UTC)
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
    const url = `/api/clients/dashboard/kpis?tz=${encodeURIComponent(tz)}`;

    const kpiRes = await fetchWithAuth(url);
    if (kpiRes.ok) {
      const k: KPIs = await kpiRes.json();
      (root.querySelector('[data-kpi="att"]') as HTMLElement).textContent   = String(k.asistenciaHoy ?? 0);
      (root.querySelector('[data-kpi="patrol"]') as HTMLElement).textContent = String(k.rondasHoy ?? 0);
      (root.querySelector('[data-kpi="visit"]') as HTMLElement).textContent  = String(k.visitasHoy ?? 0);
    } else {
      // opcional: log/handle no-ok
      console.warn('KPIs fetch no OK', kpiRes.status);
    }
  } catch (e) {
    // silenciar, que no rompa el dashboard
    console.error('Error obteniendo KPIs', e);
  }

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

  // Normalizar estructuras esperadas: [{ x: 'YYYY-MM-DD', y: number }]
  const norm = (arr: any[]) => (Array.isArray(arr) ? arr.map(d => ({ x: String(d?.x), y: (typeof d?.y === 'number' ? d.y : Number(d?.y || 0)) })) : []);

  const normActual = norm(dataActual);
  const normForecast = norm(dataForecast);

  // Construir conjunto de labels (union) y ordenar. Luego limitamos a los últimos `days` días.
  const labelsSet = new Set<string>();
  normActual.forEach(d => d.x && labelsSet.add(d.x));
  normForecast.forEach(d => d.x && labelsSet.add(d.x));
  let labels = Array.from(labelsSet).sort(); // ISO dates -> lexicographic order == chronological

  // si la API o la unión generaron más de `days` entradas (ej: forecast incluye futuros), tomar últimos `days`
  if (labels.length > days) {
    labels = labels.slice(labels.length - days);
  }

  // En caso de que por alguna razón labels esté vacío, intentar construir a partir de actual (fallback)
  if (labels.length === 0 && normActual.length > 0) {
    labels = normActual.map(d => d.x).slice(-days);
  } else if (labels.length === 0 && normForecast.length > 0) {
    labels = normForecast.map(d => d.x).slice(-days);
  }

  // Mapear por fecha para alinear valores
  const mapFrom = (arr: { x: string; y: number }[]) => {
    const m = new Map<string, number>();
    arr.forEach(p => { if (p && p.x) m.set(p.x, Number.isFinite(Number(p.y)) ? Number(p.y) : 0); });
    return m;
  };
  const mActual = mapFrom(normActual);
  const mForecast = mapFrom(normForecast);

  const valuesActual = labels.map(l => mActual.has(l) ? mActual.get(l)! : 0);
  const valuesForecast = labels.map(l => mForecast.has(l) ? mForecast.get(l)! : 0);

  if (!chVisit){
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
        // params es array con los puntos alineados por eje x
        if (!Array.isArray(params) || params.length === 0) return '';
        const date = params[0].axisValue;
        const lines = params.map((p: any) => `${p.marker} ${p.seriesName}: ${p.value ?? 0}`);
        return `<b>${date}</b><br/>${lines.join('<br/>')}`;
      }
    },
    grid: { left: 40, right: 16, top: 48, bottom: 32 },
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


// helpers: formatea 'YYYY-MM-DD' -> '12-ene' (sin punto)
const locale = navigator.language || 'es';
const monthFormatter = new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'short' });

function toDateFromIsoDay(isoDay: string): Date | null {
  if (!isoDay) return null;
  // construimos un Date estable a medianoche; añadir 'T00:00:00' evita ambigüedad
  const d = new Date(`${isoDay}T00:00:00`);
  return Number.isNaN(d.getTime()) ? null : d;
}

function shortLabelFromIso(isoDay: string): string {
  const d = toDateFromIsoDay(isoDay);
  if (!d) return isoDay ?? '';
  // Intl returns e.g. "12 ene." en es; quitamos el punto final y reemplazamos espacio por '-'
  const txt = monthFormatter.format(d).replace('.', '');
  // convertir "12 ene" -> "12-ene"
  return txt.replace(/\s+/, '-');
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


  // -------- Inicializar los 3 gráficos horarios (Asistencias, Rondas, Visitas) --------
  const today = new Date();
  const todayISO = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;

  try {
    initThreeMetrics(root, todayISO, { refreshMs: 60_000 }); // refresca cada 60s (opcional)
  } catch (e) {
    console.warn('[client-dashboard] initThreeMetrics failed', e);
  }

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