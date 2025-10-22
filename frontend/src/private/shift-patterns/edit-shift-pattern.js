import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

// --- Eliminar patrón de turno ---
async function onClickDeleteShiftPattern(e) {
  const id = e.target.getAttribute('data-id');
  if (!id) return;

  if (!confirm('¿Seguro que quieres eliminar este patrón de turno? Esta acción no se puede deshacer.')) return;

  try {
    const res = await fetchWithAuth(`/api/shift-patterns/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    navigateTo('/private/shift-patterns/table-view');
  } catch (e2) {
    alert(e2.message || 'No se pudo eliminar el patrón de turno');
  }
}

// --- Volver al listado ---
function onClickBack(e) {
  e.preventDefault();
  navigateTo('/private/shift-patterns/table-view');
}

function bindEditShiftPattern() {
  qs('#deleteShiftPatternBtn')?.addEventListener('click', onClickDeleteShiftPattern);
  qs('[data-path="/private/shift-patterns/table-view"]')?.addEventListener('click', onClickBack);
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/shift-patterns/table-view');
  });
}

(function init() {
  bindEditShiftPattern();
})();