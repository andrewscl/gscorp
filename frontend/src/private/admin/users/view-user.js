import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');
const reInviteBtn = qs('.btn-primary');

const cancelViewUser = () => {
    setTimeout(() => navigateTo('/private/users/table-view', true), 1000);
}

const reInviteUser = async (e) => {
    if (e) e.preventDefault();

    reInviteBtn.disabled = true;

    const externalId = qs('#viewUserExternalId')?.value?.trim();

    if (!externalId) {
        displayAlert(alertError, 'No se pudo obtener el identificador único del usuario.', 1500);
        reInviteBtn.disabled = false;
        return;
    }

    try {
        const res = await fetchWithAuth(`/api/users/${externalId}/resend-invite`, {
            method: 'POST',
            headers: { 'accept': 'application/json' }
        });

        if (!res.ok) {
            displayAlert(alertError, 'No se pudo invitar al usuario.', 1500);
            reInviteBtn.disabled = false;
            return;
        }

        displayAlert(alertSuccess, 'Usuario invitado correctamente.', 1500);
        setTimeout(() => {navigateTo('/private/users/table-view');}, 1500);

    } catch (e) {
        console.error("el error es: ", e);
        displayAlert(alertError, 'Error al re-invitar al usuario.', 1500);
        reInviteBtn.disabled = false;
    }

}

function bindViewUser() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewUser);
    }
    const reInviteBtn = qs('.btn-primary');
    if (reInviteBtn) {
        reInviteBtn.addEventListener('click', reInviteUser);
    }
}




    
(async function init() {
    bindViewUser();
})();