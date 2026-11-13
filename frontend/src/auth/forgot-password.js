import { navigateTo } from '../navigation-handler.js';
import { displayAlert } from '../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

(function init() {
  bindForgotPasswordForm();
})();

function bindForgotPasswordForm() {
    const processBtn = qs('.btn-primary');
    if (processBtn) {
        processBtn.addEventListener('click', processForgotPassword);
    }
}

const processForgotPassword = async () => {
    const processBtn = qs('.btn-primary');
}