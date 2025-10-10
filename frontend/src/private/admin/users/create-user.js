import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

console.log("create-user.js cargado");

const qs  = (s) => document.querySelector(s);
const qsa = (s) => Array.from(document.querySelectorAll(s));

/* --- Modal --- */
function openModal() {
  const m = qs('#createUserModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
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
  qs('#clientChoices')?.replaceChildren();
}

/* --- Roles --- */
async function fetchRoles() {
  const res = await fetchWithAuth('/api/roles/all'); // deja este endpoint si así está en tu backend
  if (!res.ok) throw new Error('No se pudieron cargar los roles');
  return res.json(); // [{ id, name }]
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

/* --- Clients --- */
async function fetchClients() {
  const res = await fetchWithAuth('/api/clients/all'); // deja este endpoint si así está en tu backend
  if (!res.ok) throw new Error('No se pudieron cargar los clientes');
  return res.json(); // [{ id, name }]
}

function renderClients(clients) {
  const box = qs('#clientChoices');
  box.innerHTML = '';
  clients.forEach(c => {
    const id = `client_${c.id}`;
    const label = document.createElement('label');
    label.className = 'client-option';
    label.innerHTML = `
      <input type="checkbox" name="clientId" value="${c.id}" id="${id}"/>
      <span>${c.name}</span>
    `;
    box.appendChild(label);
  });
}


/* --- Abrir modal y cargar roles --- */
async function onClickCreate() {
  try {
    openModal();
    const roles = await fetchRoles();
    renderRoles(roles);
    const clients = await fetchClients();
    renderClients(clients);
  } catch (e) {
    qs('#createUserError').textContent = e.message;
  }
}

/* --- Crear usuario --- */
async function onSubmitCreate(e) {
  e.preventDefault();
  const username = qs('#newUsername')?.value.trim();
  const mail = qs('#newMail')?.value.trim();
  const password = qs('#newPassword')?.value;
  const roleIds = qsa('input[name="roleId"]:checked').map(i => Number(i.value));

  const err = qs('#createUserError');
  const ok  = qs('#createUserOk');
  err.textContent = '';
  ok.style.display = 'none';

  try {
    const res = await fetchWithAuth('/api/users/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ username, mail, password, roleIds })
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

/* --- Bindings --- */
function bindModal() {
  qs('#createUserBtn')?.addEventListener('click', onClickCreate);
  qs('#closeCreateUser')?.addEventListener('click', closeModal);
  qs('#cancelCreateUser')?.addEventListener('click', closeModal);
  qs('#createUserForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

/* --- init --- */
(function init() {
  bindModal();
})();
