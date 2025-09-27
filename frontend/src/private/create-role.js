import { fetchWithAuth } from '../auth.js';
import { navigateTo } from '../navigation-handler.js';

console.log("create-role.js cargado");

const qs  = (s) => document.querySelector(s);

/* --- Modal --- */
function openModal() {
  const m = qs('#createRoleModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#newRole')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createRoleModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createRoleForm')?.reset();
  qs('#createRoleError').textContent = '';
  const ok = qs('#createRoleOk');
  if (ok) ok.style.display = 'none';
}

/* --- Crear rol --- */
async function onSubmitCreate(e) {
  e.preventDefault();
  const err = qs('#createRoleError');
  const ok  = qs('#createRoleOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  let role = qs('#newRole')?.value?.trim();
  if (!role) {
    if (err) err.textContent = 'El nombre del rol es obligatorio.';
    return;
  }

  // Opcional: normaliza a MAYÚSCULAS con underscores
  // role = role.replace(/\s+/g, '_').toUpperCase();

  try {
    const res = await fetchWithAuth('/api/roles/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ role, name: role })
    });

    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || `No se pudo crear el rol (HTTP ${res.status})`);
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      closeModal();
      navigateTo('/private/admin/roles'); // refresca listado
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message || 'No se pudo crear el rol.';
  }
}

/* --- Bindings --- */
function bindModal() {
  qs('#createRoleBtn')?.addEventListener('click', (ev) => {
    // por si el botón tuviera data-path
    ev.preventDefault?.();
    openModal();
  });
  qs('#closeCreateRole')?.addEventListener('click', closeModal);
  qs('#cancelCreateRole')?.addEventListener('click', closeModal);
  qs('#createRoleForm')?.addEventListener('submit', onSubmitCreate);

  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') closeModal();
  });

  // cerrar al click fuera del diálogo (si tu markup lo permite)
  qs('#createRoleModal')?.addEventListener('click', (e) => {
    if (e.target?.id === 'createRoleModal') closeModal();
  });
}

/* --- init --- */
(function init() {
  bindModal();
})();
