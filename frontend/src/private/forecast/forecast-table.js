// Módulo cliente para la tabla de forecasts (ES module).
// IMPORTS: utiliza las utilidades del SPA (fetchWithAuth y navigateTo) para
// navegar sin recargar toda la página y para llamadas autenticadas.
import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// ========== CONFIG ==========
const DEFAULT_PAGE_SIZE = 500;

// ========== Helpers ==========
function buildQuery(params){
  const esc = encodeURIComponent;
  return Object.entries(params)
    .filter(([k,v]) => v != null && String(v).trim() !== '')
    .map(([k,v]) => esc(k) + '=' + esc(v))
    .join('&');
}

function goTo(path, force = false) {
  try {
    // Usamos la navegación SPA proporcionada por navigation-handler
    navigateTo(path, force);
  } catch (e) {
    // Fallback mínimo (nunca debería pasar si navigation-handler está presente)
    console.warn('[forecast-table] navigateTo falló, usando location.href', e);
    window.location.href = path;
  }
}

async function handleDelete(path) {
  if (!confirm('¿Confirma eliminar este forecast? Esta acción no se puede deshacer.')) return;
  try {
    const res = await fetchWithAuth(path, { method: 'DELETE', credentials: 'same-origin' });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.error || `Error ${res.status}`);
    }
    // Refrescar la vista de la tabla usando navigateTo (no recarga completa)
    // Se navega a la ruta actual para recargar el fragmento.
    goTo(window.location.pathname + window.location.search, true);
  } catch (err) {
    alert('No se pudo eliminar: ' + (err.message || err));
  }
}

// ========== Event handlers iniciales ==========
function initHandlers() {
  const applyBtn = document.getElementById('applyFiltersBtn');
  if (applyBtn) {
    applyBtn.addEventListener('click', function(){
      const base = this.getAttribute('data-path') || window.location.pathname;
      const from = document.getElementById('filter-from')?.value;
      const to = document.getElementById('filter-to')?.value;
      const zone = document.getElementById('filter-zone')?.value;
      const page = document.getElementById('filter-page')?.value;
      const size = document.getElementById('filter-size')?.value || DEFAULT_PAGE_SIZE;
      const qs = buildQuery({ from, to, zone, page, size });
      const url = qs ? base + '?' + qs : base;
      goTo(url, false);
    });
  }

  const createBtn = document.getElementById('createForecastBtn');
  if (createBtn) {
    createBtn.addEventListener('click', function(){
      const path = this.getAttribute('data-path');
      if (path) goTo(path, true);
    });
  }

  // Delegación para botones de acción dentro de la tabla
  document.querySelectorAll('.action-btn').forEach(function(btn){
    btn.addEventListener('click', function(){
      const path = this.getAttribute('data-path');
      if(!path) return;
      const action = this.getAttribute('data-action') || '';
      if (action === 'delete') {
        // Llamada DELETE via fetchWithAuth para no recargar toda la página.
        handleDelete(path);
        return;
      }
      // Ver / Editar: navegar vía SPA
      goTo(path, true);
    });
  });

  // Soporte tecla Enter en inputs (ejecuta aplicar)
  ['filter-from','filter-to','filter-zone'].forEach(function(id){
    const el = document.getElementById(id);
    if(el){
      el.addEventListener('keydown', function(e){
        if(e.key === 'Enter'){
          e.preventDefault();
          applyBtn && applyBtn.click();
        }
      });
    }
  });
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