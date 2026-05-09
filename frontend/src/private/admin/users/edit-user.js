import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);

function showStatus(message, { error = false, timeout = 4000 } = {}) {
  const el = document.getElementById('editUserError');
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

  const updateBtn = qs('#updateUserBtn');
  const cancelBtn = qs('#cancelUserBtn');
  const deleteBtn = qs('#deleteUserBtn');
  if (updateBtn) updateBtn.disabled = true;
  if (cancelBtn) cancelBtn.disabled = true;
  if (deleteBtn) deleteBtn.disabled = true;

  /* definir variables */
  const userId = qs('#userId')?.value?.trim();
  const userUsername = qs('#userUsername')?.value?.trim();
  const userMail = qs('#userMail')?.value?.trim();
  const userActive = qs('#userActive')?.checked;
  const employeeId = qs('#userEmployeeId')?.value || null;
  const userTimeZone = qs('#userTimeZone')?.value?.trim();

  const payload = {
    userUsername,
    userMail,
    userActive,
    roleIds: Array.from(qs('#userRoles')?.selectedOptions || [])
                                                      .map(o => o.value).filter(Boolean),
    clientIds: Array.from(qs('#userClients')?.selectedOptions || [])
                                                      .map(o => o.value).filter(Boolean),
    EmployeeId,
    userTimeZone
  }

  try {
    console.log('Payload generado:', payload);
    const res = await fetchWithAuth(`/api/users/${userId}`, {
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
    if (cancelBtn) cancelBtn.disabled = false;
    if (deleteBtn) deleteBtn.disabled = false;

  }

}

async function deleteUser() {
  const ok = window.confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.');
  if (!ok) return;

  if (deleteBtn) deleteBtn.disabled = true;

  try {
    const res = await fetchWithAuth(`/api/users/${userId}`, { method: 'DELETE' });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || `Error al eliminar (HTTP ${res.status})`);
    }
    // navegar al listado
    navigateTo('/private/users/table-view', true);
  } catch (err) {
    console.error('Error eliminando usuario', err);
    alert('No se pudo eliminar: ' + (err.message || err));
    deleteBtn.disabled = true;
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