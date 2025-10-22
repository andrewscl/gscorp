import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

// --- Eliminar nacionalidad ---
async function onClickDeleteNationality(e) {
  const id = e.target.getAttribute('data-id');
  if (!id) return;

  if (!confirm('¿Seguro que quieres eliminar esta nacionalidad? Esta acción no se puede deshacer.')) return;

  try {
    const res = await fetchWithAuth(`/api/nationalities/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    navigateTo('/private/nationalities/table-view');
  } catch (e2) {
    alert(e2.message || 'No se pudo eliminar la nacionalidad');
  }
}

// --- Volver al listado ---
function onClickBack(e) {
  e.preventDefault();
  navigateTo('/private/nationalities/table-view');
}

function bindEditNationality() {
  qs('#deleteNationalityBtn')?.addEventListener('click', onClickDeleteNationality);
  qs('[data-path="/private/nationalities/table-view"]')?.addEventListener('click', onClickBack);
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/nationalities/table-view');
  });
}

(function init() {
  bindEditNationality();
})();