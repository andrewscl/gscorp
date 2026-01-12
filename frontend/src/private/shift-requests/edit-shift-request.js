import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);
const qsa = (s) => Array.from(document.querySelectorAll(s));

function nextIndex(tbody) {
  const rows = tbody.querySelectorAll('tr');
  let max = -1;
  rows.forEach(r => {
    const idx = parseInt(r.getAttribute('data-index'));
    if (!isNaN(idx) && idx > max) max = idx;
  });
  return max + 1;
}

function wireRemoveButtons(root = document) {
  qsa('.remove-schedule', root).forEach(btn => {
    btn.removeEventListener('click', onRemoveRow);
    btn.addEventListener('click', onRemoveRow);
  });
}

function onRemoveRow(e) {
  const row = e.target.closest('tr');
  if (row) row.remove();
}

function addScheduleRow(prefill = {}) {
  const tbody = qs('#schedulesTbody');
  const idx = nextIndex(tbody);
  const template = qs('#scheduleRowTemplate').innerHTML;
  const html = template.replace(/__IDX__/g, idx);
  const tmp = document.createElement('tbody');
  tmp.innerHTML = html;
  const row = tmp.querySelector('tr');
  row.setAttribute('data-index', idx);

  const df = row.querySelector('.input-dayfrom');
  const dt = row.querySelector('.input-dayto');
  const st = row.querySelector('.input-start');
  const et = row.querySelector('.input-end');
  const lt = row.querySelector('.input-lunch');

  if (prefill.dayFrom) df.value = prefill.dayFrom;
  if (prefill.dayTo) dt.value = prefill.dayTo;
  if (prefill.startTime) st.value = (prefill.startTime.length >= 5 ? prefill.startTime.substring(0,5) : prefill.startTime);
  if (prefill.endTime) et.value = (prefill.endTime.length >= 5 ? prefill.endTime.substring(0,5) : prefill.endTime);
  if (prefill.lunchTime) lt.value = (prefill.lunchTime.length >= 5 ? prefill.lunchTime.substring(0,5) : prefill.lunchTime);

  qs('#schedulesTbody').appendChild(row);
  wireRemoveButtons(row);
}

function collectSchedules() {
  const rows = qs('#schedulesTbody').querySelectorAll('tr');
  const schedules = [];
  rows.forEach(r => {
    const dayFrom = (r.querySelector('.input-dayfrom') || {}).value || null;
    const dayTo = (r.querySelector('.input-dayto') || {}).value || null;
    const startTime = (r.querySelector('.input-start') || {}).value || null;
    const endTime = (r.querySelector('.input-end') || {}).value || null;
    const lunchTime = (r.querySelector('.input-lunch') || {}).value || null;
    if (dayFrom || dayTo || startTime || endTime || lunchTime) {
      schedules.push({ dayFrom, dayTo, startTime, endTime, lunchTime });
    }
  });
  return schedules;
}

function showError(msg) {
  const errorBox = qs('#editShiftRequestError');
  if (!errorBox) return;
  errorBox.style.display = 'block';
  errorBox.textContent = msg;
  qs('#editShiftRequestOk').style.display = 'none';
}

function showOk(msg) {
  const okBox = qs('#editShiftRequestOk');
  if (!okBox) return;
  okBox.style.display = 'block';
  okBox.textContent = msg || 'Guardado';
  qs('#editShiftRequestError').style.display = 'none';
}

function validateForm(payload) {
  if (payload.startDate && payload.endDate) {
    const sd = new Date(payload.startDate);
    const ed = new Date(payload.endDate);
    if (sd > ed) return 'La fecha inicio no puede ser posterior a la fecha término.';
  }
  for (let i = 0; i < payload.schedules.length; i++) {
    const s = payload.schedules[i];
    if ((s.startTime && !s.endTime) || (!s.startTime && s.endTime)) {
      return 'Cada tramo debe tener hora inicio Y hora término o ninguno.';
    }
  }
  return null;
}

async function onSaveClick(e) {
  const idEl = document.querySelector('input[name="id"]');
  const id = idEl ? idEl.value : null;
  const code = qs('#shiftRequestCode')?.value || null;
  const type = qs('#shiftRequestType')?.value || null;
  const startDate = qs('#shiftRequestStartDate')?.value || null;
  const endDate = qs('#shiftRequestEndDate')?.value || null;
  const status = qs('#shiftRequestStatus')?.value || null;
  const description = qs('#shiftRequestDescription')?.value || null;

  const schedules = collectSchedules();

  const payload = {
    id: id ? Number(id) : null,
    code,
    type,
    startDate,
    endDate,
    status,
    description,
    schedules
  };

  const vErr = validateForm(payload);
  if (vErr) {
    showError(vErr);
    return;
  }

  try {
    // PUT using fetchWithAuth; expects JSON on the server side
    const url = `/api/shift-requests/${payload.id}`;
    const res = await fetchWithAuth(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      let txt = '';
      try { txt = await res.text(); } catch {}
      throw new Error(txt || `Error ${res.status}`);
    }

    showOk('Cambios guardados ✅');
    // short delay then navigate back to list
    setTimeout(() => navigateTo('/private/shift-requests/table-view'), 800);
  } catch (err) {
    console.error('save error', err);
    showError(err.message || 'Error al guardar');
  }
}



async function onDeleteShiftRequest() {
  const idEl = document.querySelector('input[name="id"]');
  const shiftRequestId = idEl ? idEl.value : null;

  // Referencias a elementos para mostrar errores y mensajes
  const errorBox = qs('#editShiftRequestError');
  const okBox = qs('#editShiftRequestOk');

  // Limpia cualquier mensaje previo en pantalla
  if (errorBox) errorBox.style.display = 'none';
  if (okBox) okBox.style.display = 'none';

  // Validación: Verifica si se tiene el ID
  if (!shiftRequestId) {
    showError('No se puede eliminar porque falta el ID de la solicitud.');
    return;
  }

  // Confirmación de eliminación
  const confirmDelete = confirm('¿Estás seguro de que deseas eliminar esta solicitud de turno? Esta acción no se puede deshacer.');
  if (!confirmDelete) return;

  try {
    // Realiza la solicitud DELETE al backend
    const url = `/api/shift-requests/${shiftRequestId}`;
    const res = await fetchWithAuth(url, { method: 'DELETE' });

    // Manejo de errores en la respuesta
    if (!res.ok) {
      const errorText = await res.text().catch(() => `Error ${res.status}`);
      throw new Error(errorText);
    }

    // Mostrar mensaje de éxito
    showOk('Solicitud de turno eliminada correctamente.');
    
    // Redirigir al listado después de 1 segundo
    setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1000);
  } catch (err) {
    console.error('delete error', err);
    showError(err.message || 'Error al eliminar la solicitud de turno.');
  }
}



function onCancelClick(e) {
  e.preventDefault();
  navigateTo('/private/shift-requests/table-view');
}

function bind() {
  qs('#addScheduleBtn')?.addEventListener('click', () => addScheduleRow({}));
  qs('#saveShiftRequestBtn')?.addEventListener('click', onSaveClick);
  qs('#cancelEditBtn')?.addEventListener('click', onCancelClick);
  qs('#deleteShiftRequestBtn')?.addEventListener('click', onDeleteShiftRequest);
  wireRemoveButtons();
  // ESC shortcut to go back
  document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape') navigateTo('/private/shift-requests/table-view');
  });
}

// auto-init when DOM ready
document.addEventListener('DOMContentLoaded', () => {
  try { bind(); } catch (e) { console.error(e); }
});