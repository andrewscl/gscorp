// Módulo cliente para la tabla de forecasts (ES module).
// IMPORTS: utiliza la utilidad de navegación del SPA.
import { navigateTo } from '../../navigation-handler.js';

// ========== CONFIG ==========
const DEFAULT_PAGE_SIZE = 500;

// Evitar doble init globalmente
if (!window.__forecastTable) window.__forecastTable = { inited: false };

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
  if (!path) return;
  try {
    if (typeof navigateTo === 'function') {
      navigateTo(path, force);
      return;
    }
  } catch (e) {
    console.warn('[forecast-table] navigateTo falló, usando location.href', e);
  }
  window.location.href = path;
}

// ========== Event handlers ==========
function onApplyClick(evt) {
  const btn = evt.currentTarget;
  const base = btn?.dataset?.path || window.location.pathname;
  const from = document.getElementById('filter-from')?.value;
  const to = document.getElementById('filter-to')?.value;
  const sizeEl = document.getElementById('filter-size');
  const size = (sizeEl && sizeEl.value) ? sizeEl.value : DEFAULT_PAGE_SIZE;

  const qs = buildQuery({ from, to, size });
  const url = qs ? base + '?' + qs : base;
  goTo(url, false);
}

// Delegación global para botones de acción dentro de la tabla
function onDocumentClick(evt) {
  const btn = evt.target.closest('.action-btn');
  if (!btn) return;

  const path = btn.dataset.path || btn.getAttribute('data-path');
  if (!path) return;

  // Solo navegación (Ver / Editar / etc.). La acción delete fue eliminada desde la vista,
  // por eso no manejamos DELETE en el frontend aquí.
  goTo(path, true);
}

// Soporte tecla Enter en inputs (ejecuta aplicar)
function onInputKeydown(evt) {
  if (evt.key !== 'Enter') return;
  const id = evt.target.id;
  if (['filter-from', 'filter-to', 'filter-zone', 'filter-size'].includes(id)) {
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
  init
};