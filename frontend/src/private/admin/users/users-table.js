import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { displayAlert } from "../../../shared/display-alert";

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const inviteUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/invite', true), 1000);
}

const createUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/create', true), 1000);
}

async function searchUser(){
    const queryText = qs('#filter-q')?.value.trim() || '';
    const status = qs('#filter-user-status')?.value.trim() || '';
    const count = qs('#count')?.value.trim() || '';

    const params = new URLSearchParams();
    if (queryText) params.append('q', queryText);
    if (status) params.append('status', status);
    //Agregar paginación por si se requiere controlar en el futuro
    params.append('page', '0');
    params.append('size', '100');
    //ensamblar url
    const baseUrl = '/private/users/table-search';
    const url = params.toString() ? `${baseUrl}?${params.toString()}` : baseUrl;

    try {
        const res = await fetchWithAuth(url, { credentials: 'same-origin'});
        if(!res.ok) throw new Error(`Error HTTP: ${res.status}`);

        const htmlResult = await res.text();

        const tBody = qs('.hs-table-container .table tbody')
        if(tBody){
            tBody.innerHTML = '';
            tBody.innerHTML = htmlResult;
        }

        const hiddenCountInput = qs('#sync-users-count');
        const headerCountSpan = qs('.count');

        if(hiddenCountInput && headerCountSpan){
            const newCount = parseInt(hiddenCountInput.value, 10) || 0;
            headerCountSpan.textContent = `${newCount} registro${newCount === 1 ? '' : 's'}`;
        }

    } catch (err) {
        console.error("No se pudo procesar la búsqueda de usuarios:", err);
    }
}

async function resendExpiredInvites() {
    const resendExpiredInvitesBtn = qs('#resendExpiredInvitesBtn');
    if (resendExpiredInvitesBtn) {
        resendExpiredInvitesBtn.disabled = true;
    }

    try {
        const res = await fetchWithAuth('/api/users/resend-expired-invites', {
            method: 'POST',
            headers: { 'accept': 'application/json' }
        });

        if (!res.ok) {
            displayAlert(alertError, 'No se pudo procesar el reenvío masivo de invitaciones.', 1500);
            if (resendExpiredInvitesBtn) resendExpiredInvitesBtn.disabled = false;
            return;
        }

        // Recuperamos el mensaje con el conteo dinámico enviado por el String.format de tu backend
        const message = await res.text(); 
        displayAlert(alertSuccess, message, 2500);
        
        // Refrescamos la tabla o redirigimos después de mostrar el éxito
        setTimeout(() => {
            navigateTo('/private/users/table-view');
        }, 2500);

    } catch (e) {
        console.error("Error en la ejecución del lote masivo: ", e);
        displayAlert(alertError, 'Ocurrió un error al intentar procesar las invitaciones expiradas.', 1500);
        if (resendExpiredInvitesBtn) resendExpiredInvitesBtn.disabled = false;
    }

}


function bindUserTable() {
    const addUserBtn = qs('#addUserBtn');
    const inviteUserBtn = qs('#inviteUserBtn');
    const searchUserBtn = qs('#searchUserBtn');
    const resendExpiredInvitesBtn = qs('#resendExpiredInvitesBtn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', createUser);
    }
    if (inviteUserBtn) {
        inviteUserBtn.addEventListener('click', inviteUser);
    }
    if (searchUserBtn) {
        searchUserBtn.addEventListener('click', searchUser);
    }
    if (resendExpiredInvitesBtn) {
        resendExpiredInvitesBtn.addEventListener('click', resendExpiredInvites);
    }
}

(function init () {
  bindUserTable();
})();