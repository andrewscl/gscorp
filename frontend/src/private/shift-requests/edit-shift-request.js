import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Editar solicitud de turno --- */
async function onSubmitEditShiftRequest(e) {
  e.preventDefault();

  const id = qs('#editShiftRequestForm')?.getAttribute('data-id') || qs('#shiftRequestId')?.value;
  const code = qs('#shiftRequestCode')?.value?.trim();
  const type = qs('#shiftRequestType')?.value?.trim();
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value;
  const weekDays = qs('#shiftRequestWeekDays')?.value?.trim() || null;
  const shiftDateTime = qs('#shiftRequestShiftDateTime')?.value || null;
  const startTime = qs('#shiftRequestStartTime')?.value || null;
  const endTime = qs('#shiftRequestEndTime')?.value || null;
  const lunchTime = qs('#shiftRequestLunchTime')?.value || null;
  const status = qs('#shiftRequestStatus')?.value?.trim();
  const description = qs('#shiftRequestDescription')?.value?.trim() || null;

  const err = qs('#editShiftRequestError');
  const ok = qs('#editShiftRequestOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  // Validaciones mínimas
  if (!code) {
    if (err) err.textContent = 'El código es obligatorio.';
    return;
  }
  if (!type) {
    if (err) err.textContent = 'El tipo es obligatorio.';
    return;
  }
  if (!startDate) {
    if (err) err.textContent = 'La fecha de inicio es obligatoria.';
    return;
  }
  if (!status) {
    if (err) err.textContent = 'El estado es obligatorio.';
    return;
  }

  // Deshabilita submit durante el PATCH/PUT
  const submitBtn = e.submitter || qs('#editShiftRequestForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth(`/api/shift-requests/edit/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code,
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
function bindEditShiftRequestForm() {
  const form = qs('#editShiftRequestForm');
  if (form) {
    // Si quieres puedes guardar el id como data-id en el form
    const id = qs('.meta-id span')?.textContent?.trim();
    if (id) form.setAttribute('data-id', id);
    form.addEventListener('submit', onSubmitEditShiftRequest);
  }
}

function bindCancelEditShiftRequest() {
  qs('.actions .vs-secondary')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-requests/table-view');
  });
}

/* --- init --- */
(function init() {
  bindEditShiftRequestForm();
  bindCancelEditShiftRequest();
})();