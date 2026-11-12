import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

  async function onSubmitInviteUser() {
    const inviteUserBtn = qs('#submit');

    const username = qs('#inviteUsername')?.value?.trim();
    const mail = qs('#inviteMail')?.value?.trim();
    const companyIds = Array.from(qs('#userCompanies')?.selectedOptions || [])
                  .map(o => Number(o.value))
                  .filter(Boolean)
    const clientIds = Array.from(qs('#userClients')?.selectedOptions || [])
                  .map(o => Number(o.value))
                  .filter(Boolean)
    const employeeId = Number(qs('#inviteEmployeeId')?.value) || null;
    const roleId = Number(qs('#inviteRoleId')?.value) || null;

    if (!username || !mail) {
        displayAlert(alertError, 'El nombre de usuario y el correo electrónico son obligatorios.', 1500);
        return;
    }

    if(inviteUserBtn) inviteUserBtn.disabled = true;

    const hasSelectedEmployee = qs('#inviteContext')?.dataset?.hasSelectedEmployee === 'true';
    const employeeExternalId = qs('#employeeExternalId')?.dataset?.employeeExternalId;

    try {
      const res = await fetchWithAuth('/api/users/invite', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username,
          mail,
          roleId,
          companyIds: companyIds.length ? companyIds : null,
          clientIds: clientIds.length ? clientIds : null,
          employeeId
        })
      });

      if (!res.ok) {
        const errorMessage = await res.text();
        displayAlert(alertError, errorMessage || 'No se pudo invitar al usuario.', 1500);
        if(inviteUserBtn) inviteUserBtn.disabled = false;
        return;
      }
      displayAlert(alertSuccess, 'Usuario invitado correctamente.', 1500);
      if (hasSelectedEmployee) {
        setTimeout(() => {navigateTo(`/private/employees/edit/${employeeExternalId}`);}, 1000);
      } else {
        setTimeout(() => {navigateTo('/private/users/table-view');}, 1000);
      }
    } catch (e2) {
      console.error("el error es: ", e2);
      displayAlert(alertError, 'Error al invitar al usuario.', 1500);
      if(inviteUserBtn) inviteUserBtn.disabled = false;
    }

  }

const cancelInviteUser = () => {
  displayAlert(alertCancel, '¿Estás seguro que deseas cancelar la invitación?', 3000);
  setTimeout(() => {navigateTo('/private/users/table-view');}, 3000);
}

/* --- Bindings --- */
function bindInviteUserForm() {
  const inviteUserBtn = qs('#submit');
  if(inviteUserBtn) {
    inviteUserBtn.addEventListener('click', onSubmitInviteUser);
  }
  const cancelBtn = qs('#cancel');
  if(cancelBtn) {
    cancelBtn.addEventListener('click', cancelInviteUser);
  }
}

/* --- init --- */
(function init() {
  bindInviteUserForm();
})();