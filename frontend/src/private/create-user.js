import { fetchWithAuth } from '../auth.js';
import { navigateTo } from '../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const qsa = (s) => Array.from(document.querySelectorAll(s));

function openModal() {
  const m = qs('#createUserModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  // focus inicial
  setTimeout(() => qs('#newUsername')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createUserModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createUserForm')?.reset();
  qs('#createUserError').textContent = '';
  qs('#createUserOk').style.display = 'none';
  qs('#roleChoices')?.replaceChildren();
}

async function fetchRoles() {
  const res = await fetchWithAuth('/private/admin/api/roles');
  if (!res.ok) throw new Error('No se pudieron cargar los roles');
  return res.json(); // [{id, name}]
}

function renderRoles(roles) {
  const box = qs('#roleChoices');
  box.innerHTML = '';
  roles.forEach(r => {
    const id = `role_${r.id}`;
    const label = document.createElement('label');
    label.className = 'role-option';
    label.innerHTML = `
      <input type="checkbox" name="roleId" value="${r.id}" id="${id}"/>
      <span>${r.name}</span>
    `;
    box.appendChild(label);
  });
}

async function onClickCreate() {
  try {
    openModal();
    const roles = await fetchRoles();
    renderRoles(roles);
  } catch (e) {
    qs('#createUserError').textContent = e.message;
  }
}

async function onSubmitCreate(e) {
  e.preventDefault();
  const username = qs('#newUsername')?.value.trim();
  const password = qs('#newPassword')?.value;
  const roleIds = qsa('input[name="roleId"]:checked').map(i => Number(i.value));

  const err = qs('#createUserError');
  const ok  = qs('#createUserOk');
  err.textContent = '';
  ok.style.display = 'none';

  try {
    const res = await fetchWithAuth('/private/admin/api/users', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ username, password, roleIds })
    });

    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || 'No se pudo crear el usuario');
    }

    ok.style.display = 'block';
    setTimeout(() => {
      closeModal();
      navigateTo('/private/admin/users'); // refresca listado
    }, 600);
  } catch (e2) {
    err.textContent = e2.message;
  }
}

function bindDelete() {
  qsa('.btn-danger[data-user-id]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-user-id');
      if (!confirm(`¿Eliminar usuario ${id}?`)) return;
      try {
        const res = await fetchWithAuth(`/private/admin/api/users/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('No se pudo eliminar');
        navigateTo('/private/admin/users');
      } catch (e) {
        alert(e.message);
      }
    });
  });
}

function bindModal() {
  qs('#createUserBtn')?.addEventListener('click', onClickCreate);
  qs('#closeCreateUser')?.addEventListener('click', closeModal);
  qs('#cancelCreateUser')?.addEventListener('click', closeModal);
  qs('#createUserForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

(function init() {
  bindModal();
  bindDelete();
})();
