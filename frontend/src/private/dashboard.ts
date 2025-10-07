// dashboard.ts (solo diferencias clave)
import { echarts } from '../lib/echarts-setup';

async function fetchWithAuth(url: string, init: RequestInit = {}) {
  const token = localStorage.getItem('jwt');
  const headers = new Headers(init.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(url, { ...init, headers });
  if (res.status === 401) { localStorage.removeItem('jwt'); window.location.href = '/auth/signin'; }
  return res;
}

type Point = { x: string; y: number };

export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#dashboard-root') as HTMLElement) || container;
  if (root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';

  const el = root.querySelector('#chart-attendance') as HTMLDivElement; // 👈
  if (!el) return;

  const periodo = getPeriodoISO();
  const from = `${periodo}-01`;
  const to   = lastDay(periodo);

  let data: Point[] = [];
  try {
    const res = await fetchWithAuth(
      `/api/attendance/series?from=${from}&to=${to}&action=IN` // 👈 nuevas series
    );
    if (res.ok) data = await res.json();
  } catch (e) {
    console.warn('[attendance chart] fetch error', e);
  }

  const labels = data.map(p => p.x);
  const values = data.map(p => p.y);
  const hasData = values.some(v => Number(v) > 0);

  const chart = echarts.init(el, undefined, { renderer: 'canvas' });
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', boundaryGap: false, data: labels },
    yAxis: { type: 'value' },
    series: [{ name: 'Entradas', type: 'line', smooth: true, areaStyle: {}, data: values }],
    legend: { show: true },
    graphic: hasData ? { elements: [] } : {
      elements: [{ type: 'text', left: 'center', top: 'middle',
        style: { text: 'Sin datos', fill: '#9ca3af', fontSize: 14 } }]
    }
  });

  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);
  requestAnimationFrame(() => chart.resize());

  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}

/* helpers */
function getPeriodoISO(d = new Date()) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}
function lastDay(periodo: string) {
  const [y, m] = periodo.split('-').map(Number);
  const d = new Date(y, m, 0);
  return `${periodo}-${String(d.getDate()).padStart(2, '0')}`;
}
