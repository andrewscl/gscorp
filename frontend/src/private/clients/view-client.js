import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const cancelViewClient = () => {
    setTimeout(() => navigateTo('/private/clients/table-view', true), 1000);
}

function bindViewClient() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewClient);
    }
}

(async function init() {
    bindViewClient();
})();