import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');
const inviteUserBtn = qs('#submit');

async function onSubmitInviteUser(e) {
  e.preventDefault();

  inviteUserBtn.disabled = true;

  const username = qs('#inviteUsername')?.value?.trim();
  const mail = qs('#inviteMail')?.value?.trim();

  const companyIds = Array.from(qs('#userCompanies')?.selectedOptions || [])
                .map(o => Number(o.value))
                .filter(Boolean)

  const clientIds = Array.from(qs('#userClients')?.selectedOptions || [])
                .map(o => Number(o.value))
                .filter(Boolean)

  const employeeId = Number(qs('#inviteEmployeeId')?.value) || null;
  const role = Number(qs('#inviteRoleId')?.value) || null;

  if (!username || !mail) {
      displayAlert(alertError, 'El nombre de usuario y el correo electrónico son obligatorios.', 1500);
      return;
  }

  inviteUserBtn && (inviteUserBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/users/invite', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username,
        mail,
        role,
        companyIds: companyIds.length ? companyIds : null,
        clientIds: clientIds.length ? clientIds : null,
        employeeId
      })
    });

    if (!res.ok) {
      displayAlert(alertError, 'No se pudo invitar al usuario.', 1500);
      return;
    }
    displayAlert(alertSuccess, 'Usuario invitado correctamente.', 1500);
    setTimeout(() => {navigateTo('/private/users/table-view');}, 900);
    inviteUserBtn && (inviteUserBtn.disabled = false);
  } catch (e2) {
        console.error("el error es: ", e2);
        displayAlert(alertError, 'Error al invitar al usuario.', 1500);
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