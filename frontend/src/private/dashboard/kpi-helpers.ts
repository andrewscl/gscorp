import { fetchWithAuth } from './api';

/**
 * Inicializa los KPIs del dashboard del cliente.
 * Busca elementos con atributos data-kpi (att, patrol, visit, inc) y los actualiza.
 * @param root - Elemento raíz del dashboard
 * @param from - Fecha de inicio (YYYY-MM-DD)
 * @param to - Fecha de término (YYYY-MM-DD)
 */
export async function initKpis(
  root: HTMLElement,
  from: string,
  to: string
) {
  // Define los endpoints para cada KPI
  const endpoints = {
    att: `/api/attendance/kpi?from=${from}&to=${to}`,
    patrol: `/api/patrols/kpi?from=${from}&to=${to}`,
    visit: `/api/site-supervision-visits/kpi?from=${from}&to=${to}`,
    inc: `/api/incidents/kpi?from=${from}&to=${to}`,
  };

  // Recorre cada KPI y actualiza el valor
  await Promise.all(Object.entries(endpoints).map(async ([kpi, url]) => {
    const el = root.querySelector(`[data-kpi="${kpi}"]`);
    if (!el) return;
    try {
      const res = await fetchWithAuth(url);
      if (!res.ok) throw new Error();
      const val = await res.text();
      el.textContent = val || '0';
    } catch {
      el.textContent = '–';
    }
  }));
}