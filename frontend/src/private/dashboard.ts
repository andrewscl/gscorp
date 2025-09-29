import { echarts } from '../lib/echarts-setup';

// Wrapper simple (o importa tu fetchWithAuth real)
async function fetchWithAuth(url: string, init: RequestInit = {}) {
  const token = localStorage.getItem('jwt');
  const headers = new Headers(init.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(url, { ...init, headers });
  if (res.status === 401) { localStorage.removeItem('jwt'); window.location.href = '/auth/signin'; }
  return res;
}

export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#dashboard-root') as HTMLElement) || container;
  if (root.dataset.echartsInit === '1') return;       // idempotente
  root.dataset.echartsInit = '1';

  const el = root.querySelector('#chart-ventas') as HTMLDivElement;
  if (!el) return;                                     // asegúrate que exista y tenga altura

  const empresaId = 1;
  const periodo = getPeriodoISO();
  const from = `${periodo}-01`, to = lastDay(periodo);

  const res = await fetchWithAuth(`/api/dashboard/series?metric=ventas&empresaId=${empresaId}&from=${from}&to=${to}&groupBy=day`);
  const data = res.ok ? await res.json() : [];
  const labels = data.map((p: any) => p.x);
  const values = data.map((p: any) => p.y);

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', boundaryGap: false, data: labels },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: values }]
  });

  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);
  requestAnimationFrame(() => chart.resize());

  const onUnload = () => { try { ro.disconnect(); } catch {} try { chart.dispose(); } catch {} };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}

function getPeriodoISO(d = new Date()){ return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`; }
function lastDay(periodo: string){ const [y,m]=periodo.split('-').map(Number); const d=new Date(y,m,0); return `${periodo}-${String(d.getDate()).padStart(2,'0')}`; }
