import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

// --- Eliminar cargo ---
async function onClickDeletePosition(e) {
  const id = e.target.getAttribute('data-id');
  if (!id) return;

  if (!confirm('¿Seguro que quieres eliminar este cargo? Esta acción no se puede deshacer.')) return;

  try {
    const res = await fetchWithAuth(`/api/positions/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    navigateTo('/private/positions/table-view');
  } catch (e2) {
    alert(e2.message || 'No se pudo eliminar el cargo');
  }
}

// --- Volver al listado ---
function onClickBack(e) {
  e.preventDefault();
  navigateTo('/private/positions/table-view');
}

function bindEditPosition() {
  qs('#deletePositionBtn')?.addEventListener('click', onClickDeletePosition);
  qs('[data-path="/private/positions/table-view"]')?.addEventListener('click', onClickBack);
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/positions/table-view');
  });
}

(function init() {
  bindEditPosition();
})();