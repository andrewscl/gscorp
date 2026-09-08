import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createShiftAssignment = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');
    const projectExternalId = qs('#projectExternalId')?.value || '';
}

const cancelShiftAssignment = () => {
    displayAlert(alertWarning,
                        'La nueva zona ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/sites'), 1500);
}

function bindEvents () {
    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelShiftAssignment);
    }
}

(function init() {
    bindEvents();
})();