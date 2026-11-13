import { fetchWithAuth } from '../auth.js';
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
    const cancelBtn = qs('.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelForgotPassword);
    }
}

const cancelForgotPassword = () => {
    displayAlert(alertCancel, 'La recuperación de contraseña ha sido cancelada.', 2500);
    setTimeout(() => navigateTo('/auth/signin', true), 2000);
}

const processForgotPassword = async () => {

}