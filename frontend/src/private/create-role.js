import { fetchWithAuth } from '../auth.js';
import { navigateTo } from '../navigation-handler.js';

const $ = (s) => document.querySelector(s);

function openModal() {
  const modal = $('#createRoleModal');
  modal?.classList.remove('hidden');
  modal?.setAttribute('aria-hidden', 'false');
  $('#newRole')?.focus();
}

function closeModal() {
  const modal = $('#createRoleModal');
  modal?.classList.add('hidden');
  modal?.setAttribute('aria-hidden', 'true');
  // limpia mensajes
  $('#createRoleError').textContent = '';
  $('#createRoleOk').style.display = 'none';
  $('#createRoleForm')?.reset();
}

function normalizeRole(value) {
  // Trim + uppercase con underscores
  return value.trim().replace(/\s+/g, '_').toUpperCase();
}

async function createRole(e) {
  e.preventDefault();
  const errorBox = $('#createRoleError');
  const okBox = $('#createRoleOk');
  errorBox.textContent = '';
  okBox.style.display = 'none';

  let role = $('#newRole')?.value || '';
  role = normalizeRole(role);

  if (!role) {
    errorBox.textContent = 'El nombre del rol es obligatorio.';
    return;
  }

  try {
    const resp = await fetchWithAuth('/api/roles/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ role })
    });

    if (!resp.ok) {
      const payload = await resp.json().catch(() => ({}));
      const msg = payload?.message || `Error al crear el rol (HTTP ${resp.status}).`;
      throw new Error(msg);
    }

    okBox.style.display = 'block';

    // Opcional: cierra rápido y recarga la tabla actual
    setTimeout(() => {
      closeModal();
      // vuelve a la vista actual para refrescar lista
      navigateTo('/private/admin/roles', true);
    }, 600);

  } catch (err) {
    errorBox.textContent = err.message || 'No se pudo crear el rol.';
  }
}

function wireUp() {
  $('#createRoleBtn')?.addEventListener('click', openModal);
  $('#closeCreateRole')?.addEventListener('click', closeModal);
  $('#cancelCreateRole')?.addEventListener('click', closeModal);
  $('#createRoleForm')?.addEventListener('submit', createRole);

  // Cerrar al presionar ESC o click fuera del diálogo
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });
  $('#createRoleModal')?.addEventListener('click', (e) => {
    if (e.target?.id === 'createRoleModal') closeModal();
  });
}

document.addEventListener('DOMContentLoaded', wireUp);
