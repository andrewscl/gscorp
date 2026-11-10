import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { displayAlert } from "../../../shared/display-alert";

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const createRole = () => {
    navigateTo('/private/admin/roles/create');
}

(function init () {
    bindEvents();
})();

function bindEvents() {
    const addRoleBtn = qs('#addRoleBtn');
    if (addRoleBtn) {
        addRoleBtn.addEventListener('click', createRole);
    }
}