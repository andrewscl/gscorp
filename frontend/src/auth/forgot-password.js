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
    const processBtn = qs('#forgotPasswordBtn');
    if (processBtn) {
        processBtn.addEventListener('click', processForgotPassword);
    }
    const cancelBtn = qs('#cancelRecovery');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelForgotPassword);
    }
}

const cancelForgotPassword = () => {
    displayAlert(alertCancel, 'La recuperación de contraseña ha sido cancelada.', 2000);
    setTimeout(() => navigateTo('/auth/signin', true), 2000);
}

const processForgotPassword = async () => {
    const processBtn = qs('#forgotPasswordBtn');
    const cancelBtn = qs('#cancelRecovery');

    const usernameOrEmail = qs('#usernameOrEmail')?.value.trim();
    if (!usernameOrEmail) {
        displayAlert(alertError, 'Debe ingresar un nombre de usuario o correo electrónico.', 2000);
        return;
    }
    processBtn.disabled = true;
    cancelBtn.disabled = true;

    try {
        const url = '/auth/forgot-password';
        const response = await fetchWithAuth(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usernameOrEmail })
        });
        if (!response.ok) {
            throw new Error('No se pudo procesar la recuperación de contraseña.');
        }
        displayAlert(alertSuccess, 'Si existe una cuenta activa, se enviará un correo de recuperación.', 2000);
        setTimeout(() => { navigateTo('/auth/signin', true); }, 2000);
    } catch (error) {
        console.error('Error al procesar la recuperación de contraseña:', error);
        displayAlert(alertError, 'Ocurrió un error al procesar la recuperación de contraseña.', 3000);
        processBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}