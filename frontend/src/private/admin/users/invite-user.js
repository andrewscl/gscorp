import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-cancel');
const inviteUserBtn = qs('#submit');

async function onSubmitInviteUser(e) {
  e.preventDefault();

  inviteUserBtn.disabled = true;

  const username = qs('#inviteUsername')?.value?.trim();
  const mail = qs('#inviteMail')?.value?.trim();

  // Obtener roles seleccionados (checkboxes)
  const roleIds = Array.from(document.querySelectorAll('input[name="inviteRoleIds"]:checked'))
    .map(cb => Number(cb.value));

  // Obtener clientes seleccionados (checkboxes)
  const clientIds = Array.from(document.querySelectorAll('input[name="inviteClientIds"]:checked'))
    .map(cb => Number(cb.value));

  //Obtener empleados seleccionados (select)
  const employeeId = Number(qs('#inviteEmployeeId')?.value) || null;

  if (!username || !mail) {
    if (err) err.textContent = 'Nombre de usuario y correo electrónico son obligatorios.';
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
        roleIds: roleIds.length ? roleIds : null,
        clientIds: clientIds.length ? clientIds : null,
        employeeId
      })
    });

    if (!res.ok) {
      displayAlert(alertError, 'No se pudo invitar al usuario.', 1500);
      return;
    }

    if (ok) ok.style.display = 'block';
    displayAlert(alertSuccess, 'Usuario invitado correctamente.', 1500);
    setTimeout(() => {navigateTo('/private/users/table-view');}, 900);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
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