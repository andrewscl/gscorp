import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const cancelShiftPatternEdit = () => {
    displayAlert(alertSuccess, 'La edición del ShiftPattern ha sido cancelada.', 2000);
    setTimeout(() => {
        navigateTo('/private/shift-patterns/list');
    }, 2000);
}

const updateShiftPattern = async () => {
  const updateBtn = qs('.btn-primary');
  const cancelBtn = qs('.btn-secondary');
  const deleteBtn = qs('.btn-danger');
  if (updateBtn) updateBtn.disabled = true;
  if (cancelBtn) cancelBtn.disabled = true;
  if (deleteBtn) deleteBtn.disabled = true;
  const shiftPatternExternalId = qs('#patrolExternalId')?.value?.trim();
  if (!shiftPatternExternalId) {
    displayAlert(alertError, 'Error: No se encontró el identificador del registro.');
    return;
  }
  const name = qs('#shiftRequestName')?.value?.trim();
  const code = qs('#shiftRequestCode')?.value?.trim();
  const description = qs('#shiftRequestDescription')?.value?.trim();
  const workDaysVal = qs('#shiftRequestWorkDays')?.value || null;
  const restDaysVal = qs('#shiftRequestRestDays')?.value || null;
  const workDays = workDaysVal ? parseInt(workDaysVal,10) : null;
  const restDays = restDaysVal ? parseInt(restDaysVal,10) : null;
  const payload = { name, code, description, workDays, restDays};
  try {
    const res = await fetchWithAuth(`/api/shift-requests/${shiftPatternExternalId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res || !res.ok) {
        if (updateBtn) updateBtn.disabled = false;
        if (cancelBtn) cancelBtn.disabled = false;
        if (deleteBtn) deleteBtn.disabled = false;
        let errorMessage = 'Ocurrió un problema al enviar el formulario.';
        if(res){
            const contentType = res.headers.get('content-type');
            if(contentType && contentType.includes('application/json')) {
                const errorData = await res.json();
                errorMessage = errorData.message || errorMessage;
            }
        }
        displayAlert(alertError, `Error: ${errorMessage}`);
        return;
    }
    displayAlert(alertSuccess, 'Sistema de jornada actualizado correctamente', 2500);
    setTimeout(() => navigateTo('/private/shift-requests/list', true), 1500);
  } catch (err) {
    displayAlert(alertError, 'No se pudo guardar: ' + (err.message || err), 2500);
    if (updateBtn) updateBtn.disabled = false;
    if (cancelBtn) cancelBtn.disabled = false;
    if (deleteBtn) deleteBtn.disabled = false;
  }
}

const deleteShiftPattern = async () => {
    const ok = window.confirm('¿Eliminar este sistema de jornada? Esta acción no se puede deshacer.');
    if (!ok) return;
}

function bindEvents() {
    const updateBtn = qs('#submit');
    if (updateBtn) {
        updateBtn.addEventListener('click', updateShiftPattern);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelShiftPatternEdit);
    }
    const deleteBtn = qs('#delete');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteShiftPattern);
    }
}

(function init() {
  bindEvents();
})();