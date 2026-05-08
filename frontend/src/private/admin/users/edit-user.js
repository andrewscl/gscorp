import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);

function showStatus(message, { error = false, timeout = 4000 } = {}) {
  const el = document.getElementById('user-status');
  if (!el) return;
  el.textContent = message;
  el.style.color = error ? '#b91c1c' : '';
  if (timeout > 0) {
    setTimeout(() => {
      el.style.color = '';
    }, timeout);
  }
}

async function updateUser () {

  const endpoint = "/api/users/{id}";

  if (updateBtn) updateBtn.disabled = true;
  if (deleteBtn) deleteBtn.disabled = true;


  const payload = {}

  try {

    console.log('Payload generado:', payload);
    const res = await fetchWithAuth(endpoint, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    showStatus('Usuario actualizado correctamente',
                                        { error: false, timeout: 2000 });
    setTimeout(() => navigateTo('/private/users/table-view', true), 700);

  } catch (err) {

    console.error('Error guardando usuario', err);
    showStatus('No se pudo guardar: ' +
                    (err.message || err), { error: true, timeout: 5000 });
    if (updateBtn) updateBtn.disabled = false;
    if (deleteBtn) deleteBtn.disabled = false;

  }

}

async function deleteUser() {
  const id = btn.getAttribute('data-id') || null;
  if (!id) return;

  const ok = window.confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.');
  if (!ok) return;

  btn.disabled = true;
  try {
    const res = await fetchWithAuth(`/api/users/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || `Error al eliminar (HTTP ${res.status})`);
    }
    // navegar al listado
    navigateTo('/private/users/table-view', true);
  } catch (err) {
    console.error('Error eliminando usuario', err);
    alert('No se pudo eliminar: ' + (err.message || err));
    btn.disabled = false;
  }
}

const cancelEditUser = () => {
    showStatus('La edición del usuario a sido cancelada.',
                                        { error: false, timeout: 2000 });
    setTimeout(() => navigateTo('/private/users/table-view', true), 700);
}

function bindEditUser() {
    const updateBtn = qs('#updateUserBtn');
    if (updateBtn) {
        updateBtn.addEventListener('click', updateUser);
    }
    const cancelBtn = qs('#cancelUserBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelEditUser);
    }
    const deleteBtn = qs('#deleteUserBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteUser);
    }

}

/* --- init --- */
(function init() {
  bindEditUser();
})();