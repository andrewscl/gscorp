import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear solicitud de turno --- */
async function onSubmitCreateShiftRequest(e) {
  e.preventDefault();

  const code = qs('#shiftRequestCode')?.value?.trim();
  const siteId = qs('#shiftRequestSite')?.value;
  const type = qs('#shiftRequestType')?.value;
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value || null;
  const weekDays = qs('#shiftRequestWeekDays')?.value?.trim() || null;
  const shiftDateTime = qs('#shiftRequestShiftDateTime')?.value || null;
  const startTime = qs('#shiftRequestStartTime')?.value || null;
  const endTime = qs('#shiftRequestEndTime')?.value || null;
  const lunchTime = qs('#shiftRequestLunchTime')?.value || null;
  const status = qs('#shiftRequestStatus')?.value;
  const description = qs('#shiftRequestDescription')?.value?.trim() || null;

  const err = qs('#createShiftRequestError');
  const ok = qs('#createShiftRequestOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  // Validaciones mínimas
  if (!code) {
    if (err) err.textContent = 'El código es obligatorio.';
    return;
  }
  if (!siteId) {
    if (err) err.textContent = 'Debe seleccionar un sitio.';
    return;
  }
  if (!type) {
    if (err) err.textContent = 'Debe seleccionar un tipo.';
    return;
  }
  if (!startDate) {
    if (err) err.textContent = 'La fecha de inicio es obligatoria.';
    return;
  }
  if (!status) {
    if (err) err.textContent = 'Debe seleccionar un estado.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createShiftRequestForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shift-requests/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code,
        siteId,
        type,
        startDate,
        endDate,
        weekDays,
        shiftDateTime,
        startTime,
        endTime,
        lunchTime,
        status,
        description
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
      navigateTo('/private/shift-requests/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateShiftRequestForm() {
  qs('#createShiftRequestForm')?.addEventListener('submit', onSubmitCreateShiftRequest);
}

function bindCancelCreateShiftRequest() {
  qs('#cancelCreateShiftRequest')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-requests/table-view');
  });
}

function bindCloseCreateShiftRequest() {
  qs('#closeCreateShiftRequest')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-requests/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateShiftRequestForm();
  bindCancelCreateShiftRequest();
  bindCloseCreateShiftRequest();
})();