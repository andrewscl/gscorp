import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear turno --- */
async function onSubmitCreateShift(e) {
  e.preventDefault();

  const siteId = qs('#shiftSite')?.value;
  const startTs = qs('#shiftStartTs')?.value;
  const endTs = qs('#shiftEndTs')?.value;
  const plannedGuards = qs('#shiftPlannedGuards')?.value;

  const err = qs('#createShiftError');
  const ok = qs('#createShiftOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  // Validaciones mínimas
  if (!siteId) {
    if (err) err.textContent = 'Debe seleccionar un sitio.';
    return;
  }
  if (!startTs) {
    if (err) err.textContent = 'Debe ingresar la fecha y hora de inicio.';
    return;
  }
  if (!endTs) {
    if (err) err.textContent = 'Debe ingresar la fecha y hora de término.';
    return;
  }
  if (!plannedGuards || isNaN(plannedGuards) || plannedGuards < 1) {
    if (err) err.textContent = 'Debe ingresar una cantidad válida de guardias planificados.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createShiftForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shifts/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        siteId,
        startTs,
        endTs,
        plannedGuards: Number(plannedGuards)
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
      navigateTo('/private/shifts/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateShiftForm() {
  qs('#createShiftForm')?.addEventListener('submit', onSubmitCreateShift);
}

function bindCancelCreateShift() {
  qs('#cancelCreateShift')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shifts/table-view');
  });
}

function bindCloseCreateShift() {
  qs('#closeCreateShift')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shifts/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateShiftForm();
  bindCancelCreateShift();
  bindCloseCreateShift();
})();