import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('edit-client-account.js cargado');

const qs = (s) => document.querySelector(s);

function getAccountId() {
  const el = qs('.meta-id span');
  if (!el) return null;
  const v = el.textContent.trim();
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function showMessage(el, text, type = 'error') {
  if (!el) return;
  el.className = 'form-message';
  el.textContent = text;
  if (type === 'ok') {
    el.classList.add('form-ok');
    el.classList.remove('form-error');
  } else {
    el.classList.add('form-error');
    el.classList.remove('form-ok');
  }
}
function clearMessage(el) { if (!el) return; el.className = 'form-message'; el.textContent = ''; }

async function onSubmitEdit(e) {
  e.preventDefault();
  console.log('onSubmitEdit fired');

  const form = qs('#editClientAccountForm');
  const msgEl = qs('#editClientAccountMessage');
  const errEl = qs('#editClientAccountError');
  const okEl  = qs('#editClientAccountOk');
  const submitBtn = qs('#saveClientAccountBtn');

  clearMessage(msgEl);
  if (errEl) errEl.textContent = '';
  if (okEl) okEl.style.display = 'none';

  const id = getAccountId();
  if (!id) {
    showMessage(msgEl, 'ID de cuenta no encontrado.', 'error');
    return;
  }

  const name = qs('#clientAccountName')?.value?.trim();
  // clientId: prefer select, fallback to input[name="clientId"]
  const clientSelect = qs('#clientAccountClient');
  const clientSelectValue = clientSelect ? clientSelect.value : '';
  const clientHidden = form ? form.querySelector('input[name="clientId"]') : null;
  const clientId = clientSelectValue && clientSelectValue !== '' ? clientSelectValue : (clientHidden ? clientHidden.value : null);

  const notes = qs('#clientAccountNotes')?.value?.trim() || null;

  if (!name) {
    showMessage(msgEl, 'El nombre es obligatorio.');
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
    const res = await fetchWithAuth(`/api/client-accounts/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      if (okEl) {
        okEl.style.display = 'block';
        okEl.textContent = 'Guardado ✅';
      } else {
        showMessage(msgEl, 'Guardado correctamente.', 'ok');
      }
      setTimeout(() => navigateTo('/private/client-accounts/table-view'), 700);
      return;
    }

    // leer mensaje de error del servidor
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

    showMessage(msgEl, `No se pudo guardar: ${errText}`, 'error');
  } catch (err) {
    showMessage(msgEl, `Error de red: ${err.message || err}`, 'error');
    console.error('edit-client-account fetch error:', err);
  } finally {
    if (submitBtn) submitBtn.disabled = false;
  }
}

function onClickCancel(e) {
  e.preventDefault();
  const path = e.currentTarget.getAttribute('data-path') || '/private/client-accounts/table-view';
  navigateTo(path);
}

function bindEditClientAccount() {
  const form = qs('#editClientAccountForm');
  if (!form) {
    console.warn('edit-client-account: form no encontrado');
    return;
  }

  if (form.__editClientAccountBound) {
    console.log('edit-client-account: ya vinculado, saltando');
    return;
  }
  form.__editClientAccountBound = true;

  form.addEventListener('submit', onSubmitEdit);
  qs('#cancelEditClientAccountBtn')?.addEventListener('click', onClickCancel);

  // UX: cerrar con Escape -> volver al listado
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/client-accounts/table-view');
  });

  // Focus inicial
  setTimeout(() => qs('#clientAccountName')?.focus(), 70);

  console.log('edit-client-account: bindings aplicados');
}

(function init() {
  // Bind inmediato para soportar carga dinámica en SPA
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => bindEditClientAccount());
  } else {
    bindEditClientAccount();
  }
})();