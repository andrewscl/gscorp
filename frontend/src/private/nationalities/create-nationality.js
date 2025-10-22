import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear nacionalidad --- */
async function onSubmitCreateNationality(e) {
  e.preventDefault();

  const name = qs('#nationalityName')?.value?.trim();
  const isoCode = qs('#nationalityIsoCode')?.value?.trim() || null;

  const err = qs('#createNationalityError');
  const ok = qs('#createNationalityOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre de la nacionalidad es obligatorio.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createNationalityForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/nationalities/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        isoCode
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
      navigateTo('/private/nationalities/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateNationalityForm() {
  qs('#createNationalityForm')?.addEventListener('submit', onSubmitCreateNationality);
}

function bindCancelCreateNationality() {
  qs('#cancelCreateNationality')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/nationalities/table-view');
  });
}

function bindCloseCreateNationality() {
  qs('#closeCreateNationality')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/nationalities/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateNationalityForm();
  bindCancelCreateNationality();
  bindCloseCreateNationality();
})();