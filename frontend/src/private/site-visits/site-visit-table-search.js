import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

/*
  Cliente JS para búsquedas de visitas de supervisión.
  - Llama al endpoint REST: /api/site-supervision-visits/search
  - Actualiza la tabla, badge de cantidad y línea descriptiva sin recargar la página.
  - Usa fetchWithAuth para mantener headers/auth/CSRF según tu helper.
*/

const APPLY_PATH = '/api/site-supervision-visits/search';
const DEFAULT_VIEW = '/private/site-supervision-visits/table-view';

function qs(selector, ctx = document) { return ctx.querySelector(selector); }

function formatDateReadable(isoDateStr) {
  if (!isoDateStr) return '';
  try {
    const d = new Date(isoDateStr);
    // formateo local corto (puedes personalizar)
    return d.toLocaleDateString();
  } catch (e) {
    return isoDateStr;
  }
}

function buildRowHtml(v) {
  // Ajusta según los campos que provea tu DTO
  const id = v.id ?? '—';
  const employee = v.employeeName ?? '—';
  const site = v.siteName ?? '—';
  const when = v.visitDateTimeFormatted ?? (v.visitAt ? formatDateReadable(v.visitAt) : '—');

  return `
    <tr data-id="${id}">
      <td>${id}</td>
      <td>${employee}</td>
      <td>${site}</td>
      <td>${when}</td>
      <td class="actions">
        <button type="button" class="btn-link action-btn" title="Ver"
                data-path="/private/site-supervision-visits/show/${id}">
          <span aria-hidden="true">Ver</span>
          <span class="visually-hidden">Ver visita ${id}</span>
        </button>
        <!-- El botón Edit aparece solo si la UI/permiso lo requiere; backend controla la autorización -->
      </td>
    </tr>
  `;
}

function showError(message) {
  let err = qs('#siteVisitSearchError');
  if (!err) {
    err = document.createElement('div');
    err.id = 'siteVisitSearchError';
    err.className = 'error';
    const header = qs('.site-visit-header');
    if (header) header.insertAdjacentElement('afterend', err);
    else document.body.prepend(err);
  }
  err.textContent = message;
}

function clearError() {
  const err = qs('#siteVisitSearchError');
  if (err) err.textContent = '';
}

function updateVisitsCount(count) {
  const badge = qs('.site-visit-header .visits-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

function updateHeaderMeta(from, to) {
  // intenta actualizar el elemento .header-meta__dates si existe
  const meta = qs('.site-visit-header .header-meta__dates');
  if (!meta) return;

  if (from && to) {
    meta.textContent = `Mostrando desde ${formatDateReadable(from)} hasta ${formatDateReadable(to)}`;
  } else if (from) {
    meta.textContent = `Mostrando desde ${formatDateReadable(from)}`;
  } else if (to) {
    meta.textContent = `Mostrando hasta ${formatDateReadable(to)}`;
  } else {
    meta.textContent = '';
  }
}

function renderTable(visits) {
  const tbody = qs('.site-visit-table tbody');
  if (!tbody) return;

  tbody.innerHTML = '';

  if (!Array.isArray(visits) || visits.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="empty">No se encontraron registros para el rango seleccionado.</td></tr>';
    return;
  }

  const fragmentHtml = visits.map(v => buildRowHtml(v)).join('');
  tbody.innerHTML = fragmentHtml;
}

async function applyFiltersRequest({ from, to, clientTz, applyBtn }) {
  // Construir query params
  const params = new URLSearchParams();
  if (from) params.set('from', from);
  if (to) params.set('to', to);
  if (clientTz) params.set('clientTz', clientTz);

  const url = APPLY_PATH + (params.toString() ? `?${params.toString()}` : '');

  // UI: desactivar botón y limpiar errores
  if (applyBtn) {
    applyBtn.disabled = true;
    applyBtn.classList.add('is-loading');
  }
  clearError();

  try {
    const res = await fetchWithAuth(url, {
      method: 'GET',
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    });

    if (!res.ok) {
      const txt = await res.text().catch(() => '');
      throw new Error(txt || `Error en la búsqueda (HTTP ${res.status})`);
    }

    const json = await res.json();

    // actualizar UI
    renderTable(json.visits || []);
    updateVisitsCount(json.visitsCount ?? (json.visits ? json.visits.length : 0));
    updateHeaderMeta(json.from, json.to);

  } catch (err) {
    console.error('Error buscando visitas:', err);
    showError(err.message || 'Error al buscar visitas.');
  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const applyBtn = qs('#applyFiltersBtn');
  const fromInput = qs('#filter-from');
  const toInput = qs('#filter-to');

  if (!applyBtn || !fromInput || !toInput) return;

  // Disparar búsqueda con Enter en cualquiera de los inputs
  [fromInput, toInput].forEach(inp => {
    inp.addEventListener('keydown', (ev) => {
      if (ev.key === 'Enter') {
        ev.preventDefault();
        applyBtn.click();
      }
    });
  });

  applyBtn.addEventListener('click', async () => {
    let from = fromInput.value || '';
    let to = toInput.value || '';

    // Normalizar: si ambos vacíos -> navegar a la vista por defecto
    if (!from && !to) {
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    // Si solo uno está presente, interpretamos como búsqueda de ese día
    if (!from && to) from = to;
    if (!to && from) to = from;

    // swap si están invertidos
    try {
      const fDate = new Date(from);
      const tDate = new Date(to);
      if (!isNaN(fDate) && !isNaN(tDate) && fDate > tDate) {
        [from, to] = [to, from];
      }
    } catch (e) {
      // ignore
    }

    // client timezone (opcional pero recomendable)
    let clientTz = '';
    try {
      clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
    } catch (e) {
      clientTz = '';
    }

    await applyFiltersRequest({ from, to, clientTz, applyBtn });
  });

  // Si quieres que la tabla se cargue al iniciar con valores existentes en inputs,
  // descomenta la siguiente línea:
  // if (fromInput.value || toInput.value) applyBtn.click();
});