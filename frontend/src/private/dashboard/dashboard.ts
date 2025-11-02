import { initAttendanceChart } from './chart-attendance';
import { initIncidentsChart } from './chart-incidents';
import { initVisitsChart } from './chart-visits'; // <--- nuevo!
import { initKpis } from './kpi-helpers';

// Helpers para rango de fechas del mes actual
function getPeriodoISO(d = new Date()) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}
function lastDay(periodo: string) {
  const [y, m] = periodo.split('-').map(Number);
  const d = new Date(y, m, 0);
  return `${periodo}-${String(d.getDate()).padStart(2, '0')}`;
}

// Bootstrap principal del dashboard
export async function init({ container }: { container: HTMLElement }) {
  const root = (container.querySelector('#client-dashboard-root') as HTMLElement) || container;
  if (root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';

  const periodo = getPeriodoISO();
  const from = `${periodo}-01`;
  const to   = lastDay(periodo);

  if (typeof initKpis === 'function') {
    await initKpis(root, from, to);
  }

  await Promise.all([
    initAttendanceChart(root, from, to),
    initIncidentsChart(root, from, to),
    initVisitsChart(root, from, to) // <--- ¡ahora sí!
  ]);
}

function bootstrap() {
  const container = document.getElementById('client-dashboard-root') || document.body;
  init({ container });
}

document.addEventListener('content:loaded', bootstrap);
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bootstrap);
} else {
  bootstrap();
}