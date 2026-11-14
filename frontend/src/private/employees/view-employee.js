import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { initTabPanels} from '../../shared//tabs-handler.js'
import { displayAlert } from '../../shared/display-alert.js'

const qs = (s) => document.querySelector(s);
const alertError = qs('.alertError');
const alertSuccess = qs('.alertSuccess');

const cancelViewEmployee = () => {
    // Asumiendo una ruta similar a tu ejemplo de compañías
    setTimeout(() => navigateTo('/private/employees/table-view', true), 1000);
};

const createInviteAccount = async () => {
    const createInviteAccountBtn = qs('#createInviteAccount');
    if(createInviteAccountBtn) createInviteAccountBtn.disabled = true;

    const employeeExternalId = qs('#employeeExternalId')?.value?.trim() || null;

    if (!employeeExternalId) {
        displayAlert(alertError, 'Se requiere el employeeId para crear el usuario.');
        if(createInviteAccountBtn) createInviteAccountBtn.disabled = false;
        return;
    }
    navigateTo(`/private/users/invite-by-user/${employeeExternalId}`);
}

const resendInviteAccount = async () => {
    const resendInviteAccountBtn = qs('#resendInviteAccount');
    if(resendInviteAccountBtn) resendInviteAccountBtn.disabled = true;

    const employeeExternalId = qs('#employeeExternalId')?.value?.trim() || null;
    if (!employeeExternalId) {
        displayAlert(alertError, 'Se requiere el employeeId para reenviar la invitación.');
        if(resendInviteAccountBtn) resendInviteAccountBtn.disabled = false;
        return;
    }
    try {
        const res = await fetchWithAuth(`/api/users/resend-employee-invite`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ employeeExternalId })
        });
            if (!res || !res.ok) {
            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
                if(res){
                    const contentType = res.headers.get('content-type');
                    if(contentType && contentType.includes('application/json')) {
                        const errorData = await res.json();
                        errorMessage = errorData.message || errorMessage;
                    }
                }
                displayAlert(alertError, `Error: ${errorMessage}`);
                return;
        }
        displayAlert(alertSuccess, 'Invitación reenviada correctamente.');
    } catch (error) {
        displayAlert(alertError, `No se pudo reenviar la invitación: ${error.message}`);
    } finally {
        if(resendInviteAccountBtn) resendInviteAccountBtn.disabled = false;
    }
}

function bindViewEmployee() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewEmployee);
    }
    const createInviteAccountBtn = qs('#createInviteAccount');
    if (createInviteAccountBtn) {
        createInviteAccountBtn.addEventListener('click', createInviteAccount);
    }
    const resendInviteAccountBtn = qs('#resendInviteAccount');
    if (resendInviteAccountBtn) {
        resendInviteAccountBtn.addEventListener('click', resendInviteAccount);
    }
}

// --- Inicializador ---
(async function init() {
    initTabPanels();
    
    bindViewEmployee();
})();