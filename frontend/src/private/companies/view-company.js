import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const cancelViewCompany = () => {
    setTimeout(() => navigateTo('/private/companies/table-view', true), 1000);
}

function bindViewCompany() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewCompany);
    }
}
    
(async function init() {
    bindViewCompany();
})();