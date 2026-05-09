import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-cancel');

async function updateUser () {

  const updateBtn = qs('.btn-primary');
  const cancelBtn = qs('.btn-secondary');
  const deleteBtn = qs('.btn-danger');
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
    employeeId,
    userTimeZone
  };

  try {
    console.log('Payload generado:', payload);
    const res = await fetchWithAuth(`/api/users/${userId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    displayAlert(alertSuccess, 'Usuario actualizado correctamente', 2500);
    
    setTimeout(() => navigateTo('/private/users/table-view', true), 1500);

  } catch (err) {

    displayAlert(alertError, 'No se pudo guardar: ' + (err.message || err), 2500);

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

    displayAlert(alertSuccess, 'El usuario fue eliminado', 2500);

    setTimeout(() => navigateTo('/private/users/table-view', true), 2000);

  } catch (err) {

    displayAlert(alertError, 'No se pudo eliminar: ' + (err.message || err), 2500);

    deleteBtn.disabled = true;
  }
}

const cancelEditUser = () => {

    displayAlert(alertCancel, 'La edición del usuario a sido cancelada.', 2500);

    setTimeout(() => navigateTo('/private/users/table-view', true), 2000);
}

function bindEditUser() {
    const updateBtn = qs('.btn-primary');
    if (updateBtn) {
        updateBtn.addEventListener('click', updateUser);
    }
    const cancelBtn = qs('.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelEditUser);
    }
    const deleteBtn = qs('.btn-danger');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteUser);
    }

}

/* --- init --- */
(function init() {
  bindEditUser();
})();