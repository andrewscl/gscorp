import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { displayAlert } from "../../../shared/display-alert";

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const navigateToCloseShiftAssignment = () => {
    const shiftAssignmentStatus = qs('#shiftAssignmentStatus')?.dataset.status;
    if (shiftAssignmentStatus == 'CANCELLED' || shiftAssignmentStatus == 'FINISHED') {
        displayAlert(alertError, 'La asignación ya fue cancelada o finalizada.', 1500);
        return;
    }
    const shiftAssignmentExternalId = qs('#shiftAssignmentExternalId')?.value;
    if(shiftAssignmentExternalId ) {
        const url = `/private/shift-assignments/close/${shiftAssignmentExternalId}`
        navigateTo(url, true);
    }
}

const backToShiftAssignments = () => {
    navigateTo('/private/shift-assignments/list', true);
}

function bindEvents() {
    const backToBtn = qs('#cancel');
    if (backToBtn) {
        backToBtn.addEventListener('click', backToShiftAssignments);
    }
    const submitBtn = qs('#submit');
    if (submitBtn) {
        submitBtn.addEventListener('click', navigateToCloseShiftAssignment);
    }
}

(function init () {
  bindEvents();
})();