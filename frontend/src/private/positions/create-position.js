import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear cargo --- */
async function onSubmitCreatePosition(e) {
  e.preventDefault();

  const name = qs('#positionName')?.value?.trim();
  const description = qs('#positionDescription')?.value?.trim() || null;
  const code = qs('#positionCode')?.value?.trim() || null;
  const levelStr = qs('#positionLevel')?.value?.trim();
  const level = levelStr ? Number(levelStr) : null;
  const active = !!qs('#positionActive')?.checked;

  const err = qs('#createPositionError');
  const ok = qs('#createPositionOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre del cargo es obligatorio.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createPositionForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/positions/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        description,
        code,
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
      navigateTo('/private/positions/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreatePositionForm() {
  qs('#createPositionForm')?.addEventListener('submit', onSubmitCreatePosition);
}

function bindCancelCreatePosition() {
  qs('#cancelCreatePosition')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/positions/table-view');
  });
}

function bindCloseCreatePosition() {
  qs('#closeCreatePosition')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/positions/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreatePositionForm();
  bindCancelCreatePosition();
  bindCloseCreatePosition();
})();