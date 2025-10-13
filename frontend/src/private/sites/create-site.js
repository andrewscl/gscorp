import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createSiteModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#siteName')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createSiteModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createSiteForm')?.reset();
  const err = qs('#createSiteError');
  const ok  = qs('#createSiteOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';
}

/** (Opcional) Si el <select> viene vacío, intenta cargar proyectos vía API */
async function maybeLoadProjects() {
  const sel = qs('#siteProjectId');
  if (!sel) return;

  const hasOptions = sel.querySelectorAll('option').length > 1; // ya hay proyectos renderizados por Thymeleaf
  if (hasOptions) return;

  try {
    const res = await fetchWithAuth('/api/projects/all');
    if (!res.ok) return;

    const data = await res.json(); // [{id, name, ...}]
    // Limpia y re-render
    sel.innerHTML = '<option value="" disabled selected>Selecciona un proyecto…</option>';
    data.forEach(p => {
      const opt = document.createElement('option');
      opt.value = String(p.id);
      opt.textContent = p.name;
      sel.appendChild(opt);
    });
  } catch { /* noop */ }
}

async function onClickCreate() {
  openModal();
  await maybeLoadProjects();
}

async function onSubmitCreate(e) {
  e.preventDefault();

  const err = qs('#createSiteError');
  const ok  = qs('#createSiteOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  const projectId = Number(qs('#siteProjectId')?.value);
  const name     = qs('#siteName')?.value?.trim();
  const code     = qs('#siteCode')?.value?.trim() || null;
  const address  = qs('#siteAddress')?.value?.trim() || null;
  const latStr   = qs('#siteLat')?.value?.trim();
  const lonStr   = qs('#siteLon')?.value?.trim();
  const timeZone = qs('#siteTz')?.value?.trim() || null;
  const active   = !!qs('#siteActive')?.checked;

  // Parseo seguro de coordenadas (null si vacío)
  const lat = latStr ? Number(latStr) : null;
  const lon = lonStr ? Number(lonStr) : null;

  // Validaciones mínimas
  if (!projectId) { err && (err.textContent = 'Debes seleccionar un proyecto.'); return; }
  if (!name)      { err && (err.textContent = 'El nombre es obligatorio.'); return; }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createSiteForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/sites/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        projectId,
        name,
        address,
        lat,
        lon,
        timeZone,
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
      navigateTo('/private/sites/table-view'); // recarga el listado
    }, 600);

  } catch (e2) {
    err && (err.textContent = e2.message || 'No se pudo crear el sitio');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

function bindModal() {
  qs('#createSiteBtn')?.addEventListener('click', onClickCreate);
  qs('#closeCreateSite')?.addEventListener('click', closeModal);
  qs('#cancelCreateSite')?.addEventListener('click', closeModal);
  qs('#createSiteForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

(function init() {
  bindModal();
})();