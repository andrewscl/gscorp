import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');


async function updateRole () {
  const updateBtn = qs('.btn-primary');
  const cancelBtn = qs('.btn-secondary');
  if (updateBtn) updateBtn.disabled = true;
  if (cancelBtn) cancelBtn.disabled = true;

  const id = qs('#roleId')?.value?.trim();
  const externalId = qs('#externalId')?.value?.trim();
  const role = qs('#role')?.value?.trim();
  const accountType = qs('#roleAccountType')?.value?.trim();

  const payload = {
    id,
    externalId,
    role,
    accountType
  };

  try {
    console.log('Payload generado:', payload);
    const res = await fetchWithAuth(`/api/roles/${externalId}`, {
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
  }

}


const cancelEditRole = () => {
    displayAlert(alertCancel, 'La edición del usuario a sido cancelada.', 2500);
    setTimeout(() => navigateTo('/private/users/table-view', true), 2000);
}

function bindEditRole() {
    const updateBtn = qs('.btn-primary');
    if (updateBtn) {
        updateBtn.addEventListener('click', updateUser);
    }
    const cancelBtn = qs('.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelEditRole);
    }
}

(function init() {
  bindEditRole();
})();