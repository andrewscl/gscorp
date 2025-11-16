import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-client-account.js cargado');

const qs = (s) => document.querySelector(s);

/* Mostrar/limpiar mensajes */
function showMessage(el, text, type = 'error') {
  if (!el) return;
  el.className = 'form-message';
  if (type === 'ok') el.classList.add('form-ok');
  else el.classList.add('form-error');
  el.textContent = text;
}
function clearMessage(el) { if (!el) return; el.className = 'form-message'; el.textContent = ''; }

/* --- Crear cuenta de cliente --- */
async function onSubmitCreateAccount(e) {
  e.preventDefault();

  const nameEl = qs('#clientAccountName');
  const clientSelect = qs('#clientAccountClient');
  const form = qs('#createClientAccountForm');
  const msgEl = qs('#createClientAccountMessage');
  const submitBtn = qs('#createClientAccountForm button[type="submit"]');

  clearMessage(msgEl);

  const name = nameEl?.value?.trim();
  const clientSelectValue = clientSelect ? clientSelect.value : '';
  const clientHidden = form ? form.querySelector('input[name="clientId"]') : null;
  const clientId = clientSelectValue && clientSelectValue !== '' ? clientSelectValue : (clientHidden ? clientHidden.value : null);
  const notes = qs('#clientAccountNotes')?.value?.trim() || null;

  if (!name) {
    showMessage(msgEl, 'El nombre de la cuenta es obligatorio.');
    return;
  }
  if (!clientId) {
    showMessage(msgEl, 'Debe seleccionar un cliente.');
    return;
  }

  const payload = {
    name,
    clientId: Number(clientId),
    notes
  };

  if (submitBtn) submitBtn.disabled = true;

  try {
    // Ajusta la URL si tu backend usa /api/client-accounts en vez de /api/client-accounts/create
    const res = await fetchWithAuth('/api/client-accounts/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res) throw new Error('No response from server');

    if (res.ok) {
      showMessage(msgEl, 'Cuenta creada correctamente. Redirigiendo…', 'ok');
      setTimeout(() => navigateTo('/private/client-accounts/table-view'), 700);
      return;
    }

    // manejar error del servidor
    let errText = `Error ${res.status}`;
    try {
      const json = await res.json();
      if (json && json.message) errText = json.message;
      else if (typeof json === 'string') errText = json;
      else errText = JSON.stringify(json);
    } catch (_) {
      const txt = await res.text().catch(() => '');
      if (txt) errText = txt;
    }
    showMessage(msgEl, `No se pudo crear la cuenta: ${errText}`);
  } catch (err) {
    showMessage(msgEl, `Error de red: ${err.message || err}`);
    console.error('create-client-account fetch error:', err);
  } finally {
    if (submitBtn) submitBtn.disabled = false;
  }
}

/* Bindings (estilo create-client.js) */
function bindForm() {
  const form = qs('#createClientAccountForm');
  if (!form) {
    console.warn('create-client-account: form no encontrado');
    return;
  }

  // Evitar rebind si se carga el script varias veces
  if (form.__ccaBound) return;
  form.__ccaBound = true;

  form.addEventListener('submit', onSubmitCreateAccount);

  // Cancel button (puede ser <button data-path> o .btn-secondary)
  const cancel = qs('.btn-secondary');
  if (cancel) {
    cancel.addEventListener('click', (ev) => {
      ev.preventDefault();
      const path = cancel.getAttribute('data-path') || cancel.getAttribute('href') || '/private/client-accounts/table-view';
      navigateTo(path);
    });
  }

  // UX: focus
  setTimeout(() => qs('#clientAccountName')?.focus(), 50);

  console.log('create-client-account: bindings aplicados');
}

/* Init inmediato (no depender de DOMContentLoaded del documento raíz, para SPA) */
(function init() {
  bindForm();
})();