import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createProjectModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#projectName')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createProjectModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createProjectForm')?.reset();
  const err = qs('#createProjectError');
  const ok  = qs('#createProjectOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';
}

/** (Opcional) Si el <select> viene vacío, intenta cargar clientes vía API */
async function maybeLoadClients() {
  const sel = qs('#projectClient');
  if (!sel) return;

  const hasOptions = sel.querySelectorAll('option').length > 1; // ya hay clientes renderizados por Thymeleaf
  if (hasOptions) return;

  try {
    const res = await fetchWithAuth('/api/clients/all');
    if (!res.ok) return;

    const data = await res.json(); // [{id, name, ...}]
    // Limpia y re-render
    sel.innerHTML = '<option value="" disabled selected>Seleccione cliente</option>';
    data.forEach(c => {
      const opt = document.createElement('option');
      opt.value = String(c.id);
      opt.textContent = c.name;
      sel.appendChild(opt);
    });
  } catch { /* noop */ }
}

async function onClickCreate() {
  openModal();
  await maybeLoadClients();
}

async function onSubmitCreate(e) {
  e.preventDefault();

  const err = qs('#createProjectError');
  const ok  = qs('#createProjectOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  const clientId = Number(qs('#projectClient')?.value);
  const name     = qs('#projectName')?.value?.trim();
  const code     = qs('#projectCode')?.value?.trim() || null;
  const description = ""; // Si tienes un campo de descripción, agrégalo aquí
  const startDate = qs('#projectStartDate')?.value;
  const endDate   = qs('#projectEndDate')?.value || null;
  const active    = !!qs('#projectActive')?.checked;

  // Validaciones mínimas
  if (!clientId) { err && (err.textContent = 'Debes seleccionar un cliente.'); return; }
  if (!name)     { err && (err.textContent = 'El nombre es obligatorio.'); return; }
  if (!startDate){ err && (err.textContent = 'La fecha de inicio es obligatoria.'); return; }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createProjectForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/projects/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        clientId,
        name,
        code,
        description,
        startDate,
        endDate,
        active
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    ok && (ok.style.display = 'block');

    setTimeout(() => {
      closeModal();
      navigateTo('/private/projects/table-view'); // recarga el listado
    }, 600);

  } catch (e2) {
    err && (err.textContent = e2.message || 'No se pudo crear el proyecto');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

function bindModal() {
  qs('#createProjectBtn')?.addEventListener('click', onClickCreate);
  qs('#closeCreateProject')?.addEventListener('click', closeModal);
  qs('#cancelCreateProject')?.addEventListener('click', closeModal);
  qs('#createProjectForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

(function init() {
  bindModal();
})();