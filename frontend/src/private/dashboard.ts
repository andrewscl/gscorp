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

type Point = { x: string; y: number };

export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#dashboard-root') as HTMLElement) || container;
  if (root.dataset.echartsInit === '1') return; // idempotente
  root.dataset.echartsInit = '1';

  const el = root.querySelector('#chart-ventas') as HTMLDivElement;
  if (!el) return; // asegúrate que el div tenga altura (CSS o inline)

  // Parámetros (tu API mock ya ignora estos valores, se dejan por contrato)
  const empresaId = 1;
  const periodo = getPeriodoISO();
  const from = `${periodo}-01`;
  const to   = lastDay(periodo);

  // Carga de datos con tolerancia a fallos
  let data: Point[] = [];
  try {
    const res = await fetchWithAuth(
      `/api/dashboard/series?metric=ventas&empresaId=${empresaId}&from=${from}&to=${to}&groupBy=day`
    );
    if (res.ok) {
      try { data = await res.json(); } catch { data = []; }
    }
  } catch (e) {
    console.warn('[dashboard] fetch error', e);
    data = [];
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
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: values }],
    // Mensaje "Sin datos" si corresponde
    graphic: hasData ? { elements: [] } : {
      elements: [{
        type: 'text',
        left: 'center', top: 'middle',
        style: { text: 'Sin datos', fill: '#9ca3af', fontSize: 14 }
      }]
    }
  });

  // Resize robusto
  const ro = new ResizeObserver(() => chart.resize());
  ro.observe(el);
  requestAnimationFrame(() => chart.resize());

  // Limpieza al salir del fragmento
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { ro.disconnect(); } catch {}
    try { chart.dispose(); } catch {}
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });
}

/* Helpers */
function getPeriodoISO(d = new Date()) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}
function lastDay(periodo: string) {
  const [y, m] = periodo.split('-').map(Number);
  const d = new Date(y, m, 0);
  return `${periodo}-${String(d.getDate()).padStart(2, '0')}`;
}
