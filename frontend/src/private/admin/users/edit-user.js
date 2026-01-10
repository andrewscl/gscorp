// Script para la vista de edición de usuario
// - Intercepta el submit del formulario y lo envía como PATCH JSON al endpoint
// - Maneja delete (DELETE) y detect TZ (Intl API)
// - Usa fetchWithAuth y navigateTo
import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

function qs(sel, ctx = document) { return ctx.querySelector(sel); }
function qsa(sel, ctx = document) { return Array.from(ctx.querySelectorAll(sel)); }

function showStatus(message, { error = false, timeout = 4000 } = {}) {
  const el = document.getElementById('user-status');
  if (!el) return;
  el.textContent = message;
  el.style.color = error ? '#b91c1c' : ''; // rojo en error, default otherwise
  if (timeout > 0) {
    setTimeout(() => {
      // restore default text color after timeout
      el.style.color = '';
    }, timeout);
  }
}

function serializeFormToDto(form) {
  // Construye el payload respetando los nombres del UserUpdateDto
  const usernameEl = qs('#user-username', form);
  const mailEl = qs('#user-mail', form);
  const activeEl = qs('#user-active', form);
  const rolesEl = qs('#user-roleIds', form);
  const clientsEl = qs('#user-clientIds', form);
  const employeeEl = qs('#user-employeeId', form);
  const tzEl = qs('#user-timeZone', form);

  const payload = {};

  // Strings: usar null si vacío para indicar "clear" o no definido según tu API
  if (usernameEl) {
    const v = String(usernameEl.value ?? '').trim();
    payload.username = v === '' ? null : v;
  }

  if (mailEl) {
    const v = String(mailEl.value ?? '').trim();
    payload.mail = v === '' ? null : v;
  }

  if (activeEl) {
    payload.active = !!activeEl.checked;
  }

  if (rolesEl) {
    const vals = Array.from(rolesEl.selectedOptions).map(o => o.value).filter(v => v !== '').map(Number);
    payload.roleIds = vals;
  }

  if (clientsEl) {
    const vals = Array.from(clientsEl.selectedOptions).map(o => o.value).filter(v => v !== '').map(Number);
    payload.clientIds = vals;
  }

  if (employeeEl) {
    const v = employeeEl.value;
    // si está vacío -> enviar null (limpiar); si tiene valor -> number
    payload.employeeId = (v === '' || v == null) ? null : Number(v);
  }

  if (tzEl) {
    const v = String(tzEl.value ?? '').trim();
    payload.timeZone = v === '' ? null : v;
  }

  return payload;
}

async function handleSave(form) {
  const endpoint = form.dataset.userEndpoint || form.getAttribute('data-user-endpoint');
  if (!endpoint) {
    showStatus('Endpoint del usuario no configurado', { error: true, timeout: 0 });
    return;
  }

  const saveBtn = qs('#saveUserBtn', form);
  const deleteBtn = qs('#deleteUserBtn', form);
  if (saveBtn) saveBtn.disabled = true;
  if (deleteBtn) deleteBtn.disabled = true;

  try {
    const payload = serializeFormToDto(form);

    // Enviar PATCH con JSON (ajusta a PUT si tu API lo requiere)
    const res = await fetchWithAuth(endpoint, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      // intenta parsear JSON con mensaje, si no, texto plano
      let body;
      try { body = await res.json(); } catch (e) { body = { message: await res.text().catch(() => '') }; }
      const msg = body?.message || body?.error || `Error al guardar (HTTP ${res.status})`;
      throw new Error(msg);
    }

    showStatus('Usuario actualizado correctamente', { error: false, timeout: 2000 });

    // Opcional: navegar de vuelta al listado o recargar la vista detalle
    // Aquí navegamos al listado tras una pequeña espera para que el usuario vea el mensaje
    setTimeout(() => navigateTo('/private/users/table-view', true), 700);
  } catch (err) {
    console.error('Error guardando usuario', err);
    showStatus('No se pudo guardar: ' + (err.message || err), { error: true, timeout: 5000 });
    if (saveBtn) saveBtn.disabled = false;
    if (deleteBtn) deleteBtn.disabled = false;
  }
}

async function handleDelete(btn) {
  const id = btn.getAttribute('data-id') || null;
  if (!id) return;

  const ok = window.confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.');
  if (!ok) return;

  btn.disabled = true;
  try {
    const res = await fetchWithAuth(`/api/users/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || `Error al eliminar (HTTP ${res.status})`);
    }
    // navegar al listado
    navigateTo('/private/users/table-view', true);
  } catch (err) {
    console.error('Error eliminando usuario', err);
    alert('No se pudo eliminar: ' + (err.message || err));
    btn.disabled = false;
  }
}

function initDetectTz(form) {
  const btn = qs('#detect-user-tz', form);
  const tzInput = qs('#user-timeZone', form);
  if (!btn || !tzInput) return;
  btn.addEventListener('click', () => {
    try {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
      if (tz) {
        tzInput.value = tz;
        showStatus(`Zona detectada: ${tz}`, { error: false, timeout: 2000 });
      } else {
        showStatus('No se pudo detectar la zona en este navegador', { error: true, timeout: 4000 });
      }
    } catch (e) {
      console.warn('Detect TZ failed', e);
      showStatus('Error detectando zona', { error: true, timeout: 4000 });
    }
  });
}

function attachDeleteHandler(form) {
  const btn = qs('#deleteUserBtn', form);
  if (!btn) return;
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    handleDelete(btn);
  });
}

function attachFormHandler() {
  const form = document.getElementById('editUserForm');
  if (!form) return;

  // Intercept submit
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    handleSave(form);
  });

  // Detect TZ button
  initDetectTz(form);

  // Delete handler
  attachDeleteHandler(form);
}

// Auto-init
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', attachFormHandler);
} else {
  attachFormHandler();
}

// Exports para tests o uso programático
export {
  serializeFormToDto,
  handleSave,
  handleDelete,
  attachFormHandler
};