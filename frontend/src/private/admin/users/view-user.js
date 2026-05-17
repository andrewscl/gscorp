import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-cancel');

const cancelViewUser = () => {
    setTimeout(() => navigateTo('/private/users/table-view', true), 1000);
}

function bindViewUser() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewUser);
    }
}
    
(async function init() {
    bindViewUser();
})();