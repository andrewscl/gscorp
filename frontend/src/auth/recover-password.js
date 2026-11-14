import { fetchWithAuth } from '../auth.js';
import { displayAlert } from '../shared/display-alert.js';
import { navigateTo } from '../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');


async function definePassword() {
    const definePasswordBtn = qs('#definePasswordBtn');
    const newPassword = qs('#newPassword').value;
    const confirmPassword = qs('#confirmPassword').value;
    const token = qs('#token').value;

    if (newPassword !== confirmPassword) {
        alert('Las contraseñas no coinciden');
        return;
    }
    if (!newPassword || !confirmPassword) {
        alert('Por favor ingresa y confirma tu contraseña');
        return;
    }
    if (newPassword.length < 6) {
        alert('La contraseña debe tener al menos 6 caracteres');
        return;
    }
    if (definePasswordBtn) {
        definePasswordBtn.disabled = true;
    }
    try {
        const response = await fetchWithAuth('/auth/set-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, password: newPassword })
        });
        if (!response.ok) {
            if (definePasswordBtn) {
                definePasswordBtn.disabled = false;
            }

            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            const contentType = response.headers.get('content-type');
            if(contentType && contentType.includes('application/json')) {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }
        displayAlert(alertSuccess, 'Contraseña definida correctamente');
        navigateTo('/auth/signin');
    } catch (error) {
        if (definePasswordBtn) {
            definePasswordBtn.disabled = false;
        }
        displayAlert(alertError, `Error: ${error.message}`);
    }
}

function bindRecoveryPasswordForm() {
    const definePasswordBtn = qs('#definePasswordBtn');
    if(definePasswordBtn) {
        definePasswordBtn.addEventListener('click', definePassword);
    }
}

(function init() {
  bindRecoveryPasswordForm();
})();