import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear profesión --- */
async function onSubmitCreateProfession(e) {
  e.preventDefault();

  const name = qs('#professionName')?.value?.trim();
  const description = qs('#professionDescription')?.value?.trim() || null;
  const code = qs('#professionCode')?.value?.trim() || null;
  const category = qs('#professionCategory')?.value?.trim() || null;
  const levelStr = qs('#professionLevel')?.value?.trim();
  const level = levelStr ? Number(levelStr) : null;
  const active = !!qs('#professionActive')?.checked;

  const err = qs('#createProfessionError');
  const ok = qs('#createProfessionOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre de la profesión es obligatorio.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createProfessionForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/professions/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        description,
        code,
        category,
        level,
        active
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      navigateTo('/private/professions/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateProfessionForm() {
  qs('#createProfessionForm')?.addEventListener('submit', onSubmitCreateProfession);
}

function bindCancelCreateProfession() {
  qs('#cancelCreateProfession')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/professions/table-view');
  });
}

function bindCloseCreateProfession() {
  qs('#closeCreateProfession')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/professions/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateProfessionForm();
  bindCancelCreateProfession();
  bindCloseCreateProfession();
})();