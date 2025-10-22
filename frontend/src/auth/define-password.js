import { fetchWithAuth } from '../auth.js';
import { navigateTo } from '../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Definir contraseña de usuario invitado --- */
async function onSubmitDefinePassword(e) {
  e.preventDefault();

  const password = qs('#newPassword')?.value?.trim();
  const confirmPassword = qs('#confirmPassword')?.value?.trim();
  const token = qs('#invitationToken')?.value?.trim();

  const err = qs('#definePasswordError');
  const ok = qs('#definePasswordSuccess');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!password || !confirmPassword) {
    if (err) err.textContent = 'Por favor ingresa y confirma tu contraseña.';
    return;
  }
  if (password.length < 6) {
    if (err) err.textContent = 'La contraseña debe tener al menos 6 caracteres.';
    return;
  }
  if (password !== confirmPassword) {
    if (err) err.textContent = 'Las contraseñas no coinciden.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#definePasswordForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/users/set-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, password })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    if (ok) ok.style.display = 'block';
    qs('#definePasswordForm').style.display = 'none';
    setTimeout(() => {
      navigateTo('/login');
    }, 1300);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindDefinePasswordForm() {
  qs('#definePasswordForm')?.addEventListener('submit', onSubmitDefinePassword);
}

/* --- init --- */
(function init() {
  bindDefinePasswordForm();
})();