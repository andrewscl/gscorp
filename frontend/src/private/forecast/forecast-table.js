// Módulo cliente para la tabla de forecasts (ES module).
// IMPORTS: utiliza las utilidades del SPA (fetchWithAuth y navigateTo).
import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// ========== CONFIG ==========
const DEFAULT_PAGE_SIZE = 500;

// Evitar doble init
if (!window.__forecastTable) window.__forecastTable = {};
if (!window.__forecastTable.inited) window.__forecastTable.inited = false;

// ========== Helpers ==========
function buildQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v == null) return;
    const s = String(v).trim();
    if (s === '') return;
    search.set(k, s);
  });
  return search.toString();
}

function goTo(path, force = false) {
  try {
    navigateTo(path, force);
  } catch (e) {
    console.warn('[forecast-table] navigateTo falló, usando location.href', e);
    window.location.href = path;
  }
}

async function handleDelete(path) {
  if (!confirm('¿Confirma eliminar este forecast? Esta acción no se puede deshacer.')) return;
  try {
    const res = await fetchWithAuth(path, { method: 'DELETE', credentials: 'same-origin' });
    if (!res.ok) {
      // Intentar parsear JSON, si no, mostrar status
      let body = {};
      try {
        body = await res.json();
      } catch (e) {
        // no-op
      }
      throw new Error(body?.error || `Error ${res.status}`);
    }
    // Recargar la vista (SPA)
    goTo(window.location.pathname + window.location.search, true);
  } catch (err) {
    alert('No se pudo eliminar: ' + (err.message || err));
  }
}

// ========== Event handlers ==========
function onApplyClick(evt) {
  const btn = evt.currentTarget;
  const base = btn.dataset.path || window.location.pathname;
  const from = document.getElementById('filter-from')?.value;
  const to = document.getElementById('filter-to')?.value;
  const zone = document.getElementById('filter-zone')?.value;
  const page = document.getElementById('filter-page')?.value;
  const sizeEl = document.getElementById('filter-size');
  const size = (sizeEl && sizeEl.value) ? sizeEl.value : DEFAULT_PAGE_SIZE;

  const qs = buildQuery({ from, to, zone, page, size });
  const url = qs ? base + '?' + qs : base;
  goTo(url, false);
}

// Delegación global para botones de acción dentro de la tabla
function onDocumentClick(evt) {
  const btn = evt.target.closest('.action-btn');
  if (!btn) return;

  const path = btn.dataset.path || btn.getAttribute('data-path');
  const action = btn.dataset.action || btn.getAttribute('data-action') || '';

  if (!path) return;

  if (action === 'delete') {
    handleDelete(path);
    return;
  }

  // Ver / Editar / otros: navegar vía SPA
  goTo(path, true);
}

// Soporte tecla Enter en inputs (ejecuta aplicar) — delegación por keydown en container
function onInputKeydown(evt) {
  if (evt.key !== 'Enter') return;
  const id = evt.target.id;
  if (['filter-from', 'filter-to', 'filter-zone'].includes(id)) {
    evt.preventDefault();
    const applyBtn = document.getElementById('applyFiltersBtn');
    applyBtn && applyBtn.click();
  }
}

// ========== Init ==========
function initHandlers() {
  if (window.__forecastTable.inited) return;
  window.__forecastTable.inited = true;

  const applyBtn = document.getElementById('applyFiltersBtn');
  if (applyBtn) {
    applyBtn.addEventListener('click', onApplyClick);
  }

  const createBtn = document.getElementById('createForecastBtn');
  if (createBtn) {
    createBtn.addEventListener('click', function () {
      const path = this.dataset.path || this.getAttribute('data-path');
      if (path) goTo(path, true);
    });
  }

  // Delegación a nivel de documento para manejar botones que se inyecten dinámicamente
  document.addEventListener('click', onDocumentClick);

  // Keydown delegación
  document.addEventListener('keydown', onInputKeydown);
}

// ========== Init ==========
function init() {
  initHandlers();
}

// Auto-init cuando el DOM esté listo
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}

// Export para pruebas o uso programático si es necesario
export {
  buildQuery,
  goTo,
  handleDelete,
  init
};