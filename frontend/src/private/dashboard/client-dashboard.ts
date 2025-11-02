// frontend/private/client/dashboard.ts
import { echarts } from '../../lib/echarts-setup';

// Si ya tienes un fetchWithAuth TS, importa el tuyo y borra este helper:
async function fetchWithAuth(url: string, init: RequestInit = {}) {
  const token = localStorage.getItem('jwt');
  const headers = new Headers(init.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(url, { ...init, headers });
  if (res.status === 401) {
    localStorage.removeItem('jwt');
    window.location.href = '/auth/signin';
  }
  return res;
}

type Point = { x: string; y: number };
type KPIs = { asistenciaHoy: number; rondasHoy: number; incidentesAbiertos: number };

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
  const elAsis   = root.querySelector('#chart-asistencia') as HTMLDivElement;
  const elRondas = root.querySelector('#chart-rondas') as HTMLDivElement;
  const elInc    = root.querySelector('#chart-incidentes') as HTMLDivElement;

  // -------- KPIs (cards) --------
  try {
    const kpiRes = await fetchWithAuth(`/api/clients/dashboard/kpis?clientId=${clientId}`);
    if (kpiRes.ok) {
      const k: KPIs = await kpiRes.json();
      (root.querySelector('#kpi-asistencia-hoy') as HTMLElement).textContent = String(k.asistenciaHoy ?? 0);
      (root.querySelector('#kpi-rondas-hoy') as HTMLElement).textContent    = String(k.rondasHoy ?? 0);
      (root.querySelector('#kpi-incidentes') as HTMLElement).textContent    = String(k.incidentesAbiertos ?? 0);
    }
  } catch (e) {
    // silenciar, que no rompa el dashboard
  }

  // -------- Chart helpers --------
  const charts: echarts.ECharts[] = [];
  const mkChart = (el?: HTMLDivElement) => el ? echarts.init(el, undefined, { renderer: 'canvas' }) : null;
  const setNoData = (chart: echarts.ECharts | null, msg = 'Sin datos') => {
    if (!chart) return;
    chart.setOption({ graphic: { elements: [{ type: 'text', left: 'center', top: 'middle', style: { text: msg, fill: '#9ca3af', fontSize: 14 } }] } });
  };

  // -------- Asistencia (línea con área) --------
  const chAsis = mkChart(elAsis); if (chAsis) charts.push(chAsis);
  try {
    const res = await fetchWithAuth(`/api/clients/attendance/series?clientId=${clientId}&from=${from}&to=${to}&action=IN`);
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

  // -------- Rondas (barra apilada: completadas vs esperadas) --------
  const chRondas = mkChart(elRondas); if (chRondas) charts.push(chRondas);
  try {
    const res = await fetchWithAuth(`/api/clients/patrol/compliance?clientId=${clientId}&from=${from}&to=${to}`);
    if (res.ok) {
      const data: { x: string; expected: number; completed: number }[] = await res.json().catch(() => []);
      const labels = data.map(d => d.x);
      const expected = data.map(d => d.expected);
      const completed = data.map(d => d.completed);
      chRondas?.setOption({
        tooltip: { trigger: 'axis' },
        legend: {},
        grid: { left: 40, right: 16, top: 24, bottom: 32 },
        xAxis: { type: 'category', data: labels },
        yAxis: { type: 'value' },
        series: [
          { name: 'Completadas', type: 'bar', data: completed },
          { name: 'Esperadas',   type: 'bar', data: expected }
        ],
        graphic: (completed.concat(expected)).some(v => Number(v) > 0) ? { elements: [] } : undefined
      });
      if (!(completed.concat(expected)).some(v => Number(v) > 0)) setNoData(chRondas);
    } else setNoData(chRondas, 'Error de datos');
  } catch { setNoData(chRondas, 'Error de datos'); }

  // -------- Incidentes (línea simple) --------
  const chInc = mkChart(elInc); if (chInc) charts.push(chInc);
  try {
    const res = await fetchWithAuth(`/api/clients/incidents/series?clientId=${clientId}&from=${from}&to=${to}&status=OPEN`);
    if (res.ok) {
      const data: Point[] = await res.json().catch(() => []);
      const labels = data.map(d => d.x);
      const values = data.map(d => d.y);
      chInc?.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 24, bottom: 32 },
        xAxis: { type: 'category', boundaryGap: false, data: labels },
        yAxis: { type: 'value' },
        series: [{ type: 'line', smooth: true, data: values }],
        graphic: values.some(v => Number(v) > 0) ? { elements: [] } : undefined
      });
      if (!values.some(v => Number(v) > 0)) setNoData(chInc);
    } else setNoData(chInc, 'Error de datos');
  } catch { setNoData(chInc, 'Error de datos'); }

  // Resize robusto
  const ro = new ResizeObserver(() => charts.forEach(c => c?.resize()));
  [elAsis, elRondas, elInc].forEach(el => el && ro.observe(el));

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
