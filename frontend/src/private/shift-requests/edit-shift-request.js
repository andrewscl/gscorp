import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
const externalId = qs('#shiftRequestExternalId')?.value;

console.log("edit-shift-request.js cargado");

async function onSaveClick(e) {
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value;
  const status = qs('#shiftRequestStatus')?.value;
  const description = qs('#shiftRequestDescription')?.value;

  const payload = { startDate, endDate, status, description};

  if (!externalId || !startDate || !endDate || !status) {
  console.log("falta información para efectuar la actualización.");
  return;
  }

  try {
    const url = `/api/shift-requests/${externalId}`;
    const res = await fetchWithAuth(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      let txt = '';
      try {
        txt = await res.text();
      } catch { throw new Error(txt || `Error ${res.status}`); }
    }

    displayAlert(alertSuccess, 'Cambios guardados ✅', 1000);
    setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1000);
  } catch (err) {
    console.error('save error', err);
    displayAlert(alertError, "Error al guardar. " + err.message, 2000);
  }
}


const shiftsUpdate = async () => {
  try {
    const url = `/api/shifts/create/${externalId}/${encodeURIComponent(clientTz)}`;
    const res = await fetchWithAuth(url, {
      method: 'POST'
    });

    if (!res.ok) {
      let txt = '';
      try { txt = await res.text(); } catch {}
      throw new Error(txt || `Error ${res.status}`);
    }

    displayAlert(alertSuccess, 'Turnos generados correctamente ✅', 1000);
  } catch (err) {
    console.error('save error', err);
    displayAlert(alertError, "Error al guardar. " + err.message, 2000);
  }

}


function onCancelClick(e) {
  displayAlert(alertWarning,
              'La edición del requerimiento ha sido cancelada', 1500);
  setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1500);
}


function bindEditShiftRequest() {
    const saveBtn = qs('#submit');
    if (saveBtn) {
        saveBtn.addEventListener('click', onSaveClick);
    }
    const shiftsUpdateBtn = qs('#btnShiftsUpdate');
    if (shiftsUpdateBtn) {
        shiftsUpdateBtn.addEventListener('click', shiftsUpdate);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', onCancelClick);
    }
}


(function init() {
  bindEditShiftRequest();

})();