import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { displayAlert } from "../../../shared/display-alert";

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');


const submitShiftAssignment = async () => {
    const cancelBtn = qs('#cancel');
    const submitBtn = qs('#submit');
    const assignmentStatus = qs('#shiftAssignmentStatus')?.value || '';
    const shiftAssignmentExternalId = qs('#shiftAssignmentExternalId')?.value || '';
    const assignmentEndDate = qs('#assignmentEndDate')?.value || '';
    const assignedAtDate = qs('#assignedAtDate')?.value || '';
    const assignedUntilDate = qs('#assignedUntilDate')?.value || '';
    const reason = qs('#reason')?.value || '';
    console.log('assignmentEndDate: ', shiftAssignmentExternalId);
    console.log('assignmentEndDate: ', assignmentEndDate);
    console.log('assignedAtDate: ', assignedAtDate);
    console.log('assignedUntilDate: ', assignedUntilDate);
    if (!shiftAssignmentExternalId) {
        displayAlert(alertError, 'No existe shiftAssignmentExternalId.', 1500);
        return;
    } 
    if (!assignmentStatus || !assignmentEndDate) {
        displayAlert(alertError, 'Por favor, complete la fecha y estado requeridos.', 1500);
        return;
    }
    const isBeforeStart = (assignedAtDate && assignedAtDate < assignedAtDate);
    const isAfterUntil = (assignedUntilDate && assignedAtDate > assignedUntilDate);
    if (isBeforeStart || isAfterUntil){
        displayAlert(alertError, 'La fecha de cierre de la asignación esta fuera del rango permitido.', 1500);
        return;
    }
    if(cancelBtn) cancelBtn.disabled = true;
    if(submitBtn) submitBtn.disabled = true;
    try {
        const payload = {
            status: assignmentStatus,
            endAssignmentDate: assignmentEndDate,
            reason: reason
        };
        const res = await fetchWithAuth(`/api/shift-assignments/close/${shiftAssignmentExternalId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!res || !res.ok) {
            if(cancelBtn) cancelBtn.disabled = false;
            if(submitBtn) submitBtn.disabled = false;
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
        displayAlert(alertSuccess, 'La asignación fue procesada correctamente.', 2000);
        setTimeout(() => {
            navigateTo('/private/employee-terminations/list');
        }, 2000);
    } catch (error) {
        console.error(`[onCloseShiftAssignment] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        if(cancelBtn) cancelBtn.disabled = false;
        if(submitBtn) submitBtn.disabled = false;
    }
}

const backToShiftAssignments = () => {
    displayAlert(alertCancel, 'El cierre de la asignación ha sido cancelada.', 1500);
    const shiftAssignmentExternalId = qs('#shiftAssignmentExternalId')?.value;
    if(shiftAssignmentExternalId ) {
        const url = `/private/shift-assignments/edit/${shiftAssignmentExternalId}`
        setTimeout(()=> navigateTo(url, true), 1500);
    }
}

function bindEvents() {
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', backToShiftAssignments);
    }
    const submitBtn = qs('#submit');
    if (submitBtn) {
        submitBtn.addEventListener('click', submitShiftAssignment);
    }
}

(function init () {
  bindEvents();
})();