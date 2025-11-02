// src/private/dashboard/dashboard.ts

import { initAttendanceChart } from './chart-attendance';
import { initIncidentsChart } from './chart-incidents';
import { initKpis } from './kpi-helpers'; // Si tienes lógica para KPIs, opcional

// Helpers para rango de fechas del mes actual
function getPeriodoISO(d = new Date()) {
  // Devuelve el año-mes en formato 'YYYY-MM'
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}
function lastDay(periodo: string) {
  // Devuelve el último día de un mes 'YYYY-MM' como 'YYYY-MM-DD'
  const [y, m] = periodo.split('-').map(Number);
  const d = new Date(y, m, 0);
  return `${periodo}-${String(d.getDate()).padStart(2, '0')}`;
}

// Bootstrap principal del dashboard
export async function init({ container }: { container: HTMLElement }) {
  // Busca el elemento raíz del dashboard
  const root = (container.querySelector('#client-dashboard-root') as HTMLElement) || container;

  // Previene doble inicialización
  if (root.dataset.echartsInit === '1') return;
  root.dataset.echartsInit = '1';

  // Calcula el rango del mes actual
  const periodo = getPeriodoISO();
  const from = `${periodo}-01`;
  const to   = lastDay(periodo);

  // Inicializa los KPIs (si tienes lógica para ellos)
  if (typeof initKpis === 'function') {
    await initKpis(root, from, to);
  }

  // Inicializa los gráficos principales en paralelo
  await Promise.all([
    initAttendanceChart(root, from, to),
    initIncidentsChart(root, from, to)
    // ...agrega aquí más gráficos si necesitas
  ]);
}

// Bootstrap seguro, tanto para SPA como para carga directa
function bootstrap() {
  const container = document.getElementById('client-dashboard-root') || document.body;
  init({ container });
}

// Re-inicializa si tu SPA inserta el fragmento dinámicamente
document.addEventListener('content:loaded', bootstrap);

// Fallback: inicializa al cargar el DOM (para carga directa)
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bootstrap);
} else {
  bootstrap();
}