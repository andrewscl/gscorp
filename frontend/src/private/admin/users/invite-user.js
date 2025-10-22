import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Invitar usuario --- */
async function onSubmitInviteUser(e) {
  e.preventDefault();

  const username = qs('#inviteUsername')?.value?.trim();
  const mail = qs('#inviteMail')?.value?.trim();
  const roleIds = qs('#inviteRoleIds')?.value?.trim()
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
    .map(Number);
  const clientIds = qs('#inviteClientIds')?.value?.trim()
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
    .map(Number);

  const err = qs('#inviteUserError');
  const ok = qs('#inviteUserSuccess');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!username || !mail) {
    if (err) err.textContent = 'Nombre de usuario y correo electrónico son obligatorios.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#inviteUserForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/users/invite', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username,
        mail,
        roleIds: roleIds.length ? roleIds : null,
        clientIds: clientIds.length ? clientIds : null
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      navigateTo('/private/users/table-view');
    }, 900);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindInviteUserForm() {
  qs('#inviteUserForm')?.addEventListener('submit', onSubmitInviteUser);
}

function bindCancelInviteUser() {
  qs('#cancelInviteUser')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/users/table-view');
  });
}

function bindCloseInviteUser() {
  qs('#closeInviteUser')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/users/table-view');
  });
}

/* --- init --- */
(function init() {
  bindInviteUserForm();
  bindCancelInviteUser();
  bindCloseInviteUser();
})();