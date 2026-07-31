import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createProject = () => {
}

async function handleCompanyChange () {
  const companyExternalId = qs('#company')?.value;
  const clientSelect = qs('#company');

    clientSelect.innerHTML = '<option value="">Primero seleccione una empresa</option>';
    clientSelect.disabled = true;
    if (!companyExternalId) return;



}

const cancelCreateProject = () => {
    displayAlert(alertWarning,
                'La creación del proyecto ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/projects/list'), 1500);
}

function bindEvents () {
    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateProject);
    }
    const companySelect = qs('#company');
    if (companySelect) {
        companySelect.addEventListener('change', handleCompanyChange);
    }
}

(function init() {
    bindEvents();
})();