import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-client-account.js cargado (fetchWithAuth-only)');

const qs = (sel, root = document) => (root || document).querySelector(sel);

/* Mensajes */
function showMessage(el, text, type = 'error') {
  if (!el) return;
  el.className = 'form-message';
  if (type === 'ok') el.classList.add('form-ok');
  else el.classList.add('form-error');
  el.textContent = text;
}
function clearMessage(el) { if (!el) return; el.className = 'form-message'; el.textContent = ''; }

/* Serializa form en objeto JSON simple */
function serializeForm(form) {
  const fd = new FormData(form);
  const obj = {};
  for (const [k, v] of fd.entries()) {
    if (k === 'clientId' && v !== '') obj[k] = Number(v);
    else if (v === '') obj[k] = null;
    else obj[k] = v;
  }
  return obj;
}

/* Submit handler */
async function onSubmitCreateAccount(e) {
  e.preventDefault();
  const form = e.target;
  const submitBtn = qs('button[type="submit"]', form);
  const msgEl = qs('#createClientAccountMessage', form);

  clearMessage(msgEl);

  // Validaciones (cliente)
  const name = qs('#clientAccountName', form)?.value?.trim();

  // fallback para clientId si el select está deshabilitado (hidden input)
  const clientSelect = qs('#clientAccountClient', form);
  const clientSelectValue = clientSelect ? clientSelect.value : '';
  const clientHidden = form.querySelector('input[name="clientId"]');
  const clientId = clientSelectValue && clientSelectValue !== '' ? clientSelectValue : (clientHidden ? clientHidden.value : null);

  const notes = qs('#clientAccountNotes', form)?.value?.trim() || null;

  if (!name) {
    showMessage(msgEl, 'El nombre de la cuenta es obligatorio.');
    return;
  }
  if (!clientId) {
    showMessage(msgEl, 'Debe seleccionar un cliente.');
    return;
  }

  const payload = serializeForm(form);

  if (submitBtn) submitBtn.disabled = true;

  try {
    // URL recomendada: POST /api/client-accounts
    const res = await fetchWithAuth('/api/client-accounts/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

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

/* Bindings */
function bindForm() {
  const form = qs('#createClientAccountForm');
  if (form) {
    form.addEventListener('submit', onSubmitCreateAccount);
    console.log('Submit handler vinculado al formulario createClientAccountForm');
  } else {
    console.warn('Form createClientAccountForm no encontrado - el JS no podrá interceptar submits');
  }

  // Cancel button (puede ser <button data-path> o .btn-secondary)
  const cancel = qs('.btn-secondary');
  if (cancel) {
    cancel.addEventListener('click', (ev) => {
      ev.preventDefault();
      const path = cancel.getAttribute('data-path') || cancel.getAttribute('href') || '/private/client-accounts/table-view';
      navigateTo(path);
    });
  }
}

/* Init */
document.addEventListener('DOMContentLoaded', () => {
  bindForm();
  setTimeout(() => qs('#clientAccountName')?.focus(), 50);
});