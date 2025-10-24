import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear turnos masivos --- */
async function onSubmitCreateShiftsBulk(e) {
  e.preventDefault();

  const siteId = qs('#bulkSite')?.value;
  const startDate = qs('#bulkStartDate')?.value;
  const endDate = qs('#bulkEndDate')?.value;
  const startTime = qs('#bulkStartTime')?.value;
  const endTime = qs('#bulkEndTime')?.value;
  const plannedGuards = qs('#bulkPlannedGuards')?.value;

  // Obtener los días seleccionados como array
  const weekDays = Array.from(document.querySelectorAll('input[name="weekDays"]:checked'))
    .map(cb => cb.value);

  const err = qs('#createShiftsBulkError');
  const ok = qs('#createShiftsBulkOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  // Validaciones mínimas
  if (!siteId) {
    if (err) err.textContent = 'Debe seleccionar un sitio.';
    return;
  }
  if (!startDate) {
    if (err) err.textContent = 'Debe ingresar una fecha de inicio.';
    return;
  }
  if (!endDate) {
    if (err) err.textContent = 'Debe ingresar una fecha de término.';
    return;
  }
  if (!startTime) {
    if (err) err.textContent = 'Debe ingresar la hora de inicio.';
    return;
  }
  if (!endTime) {
    if (err) err.textContent = 'Debe ingresar la hora de término.';
    return;
  }
  if (!plannedGuards || isNaN(plannedGuards) || plannedGuards < 1) {
    if (err) err.textContent = 'Debe ingresar una cantidad válida de guardias por turno.';
    return;
  }
  if (!weekDays.length) {
    if (err) err.textContent = 'Debe seleccionar al menos un día de la semana.';
    return;
  }

  const submitBtn = e.submitter || qs('#createShiftsBulkForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shifts/bulk-create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        siteId,
        startDate,
        endDate,
        weekDays,
        startTime,
        endTime,
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
    }, 700);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateShiftsBulkForm() {
  qs('#createShiftsBulkForm')?.addEventListener('submit', onSubmitCreateShiftsBulk);
}

function bindCancelCreateShiftsBulk() {
  qs('#cancelCreateShiftsBulk')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shifts/table-view');
  });
}

function bindCloseCreateShiftsBulk() {
  qs('#closeCreateShiftsBulk')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shifts/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateShiftsBulkForm();
  bindCancelCreateShiftsBulk();
  bindCloseCreateShiftsBulk();
})();