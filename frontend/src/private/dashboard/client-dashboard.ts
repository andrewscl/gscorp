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



// ---------------- Reemplazo: "Visitas por día" con timeout y limpieza ----------------
const chVisit = mkChart(elVisit);
if (chVisit) charts.push(chVisit);

try {
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
  const days = 7;
  const url = `/api/site-supervision-visits/series?days=${days}&tz=${encodeURIComponent(tz)}`;

  // Llamada correcta: pasar true para usar fetchWithAuth (sin tocar fetchWithAuth ni RequestInit)
  const res = await fetchWithTimeout(url, {}, 15000, true);

  if (!res) {
    setNoData(chVisit, 'Error de datos');
    return;
  }

  if (!res.ok) {
    if (res.status === 401) {
      console.warn('Unauthorized when fetching visits series');
    } else {
      console.warn('Visits series fetch returned status', res.status);
    }
    setNoData(chVisit, 'Error de datos');
    return;
  }

  const data: { x: string; y: number | string }[] = await res.json().catch(() => []);
  const labels = data.map(d => d.x);
  const values = data.map(d => {
    const v = typeof d.y === 'number' ? d.y : Number(d.y);
    return Number.isFinite(v) ? v : 0;
  });


  if(chVisit) {
    chVisit.clear();
    chVisit.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 24, bottom: 32 },
      xAxis: { type: 'category', boundaryGap: false, data: labels },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, areaStyle: {}, data: values, color: '#0ea5e9' }],
      graphic: values.some(v => v > 0) ? { elements: [] } : undefined
    });
  } else {
    setNoData(null, 'Error de datos');
  }




  if (!values.some(v => v > 0)) setNoData(chVisit);
} catch (err: any) {
  if (err?.name === 'AbortError') {
    console.debug('Visits series fetch aborted (timeout or navigation)', err);
  } else {
    console.error('Error obteniendo series de visitas', err);
    setNoData(chVisit, 'Error de datos');
  }
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