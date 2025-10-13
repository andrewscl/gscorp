import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

// --- Eliminar cliente/usuario ---
async function onClickDeleteUser(e) {
  const id = e.target.getAttribute('data-id');
  if (!id) return;

  if (!confirm('¿Seguro que quieres eliminar este cliente? Esta acción no se puede deshacer.')) return;

  try {
    const res = await fetchWithAuth(`/api/clients/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    navigateTo('/private/clients/table-view');
  } catch (e2) {
    alert(e2.message || 'No se pudo eliminar el cliente');
  }
}

// --- Volver al listado ---
function onClickBack(e) {
  e.preventDefault();
  navigateTo('/private/clients/table-view');
}

function bindEditClient() {
  qs('#deleteUserBtn')?.addEventListener('click', onClickDeleteUser);
  qs('[data-path="/private/clients/table-view"]')?.addEventListener('click', onClickBack);
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/clients/table-view');
  });
}

(function init() {
  bindEditClient();
})();