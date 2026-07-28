import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createShiftAssignment = () => {
    navigateTo('/private/shift-assignments/create', true);
}

const cancelShiftAssignment = () => {
    displayAlert(alertWarning,
                'La asignación de turno ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/shift-assignments/list'), 1500);
}

function bindCreateShiftAssignments() {
    const createBtn = qs('#createShiftAssignmentBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const searchBtn = qs('#cancelShiftAssignmentBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', cancelShiftAssignment);
    }
}

(function init () {
  bindCreateShiftAssignments();

})();