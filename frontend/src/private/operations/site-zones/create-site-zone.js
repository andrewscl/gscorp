import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createSiteZone = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');
    const siteExternalId = qs('#siteExternalId')?.value || '';
    const name = qs('#siteZoneName')?.value || '';
    if (!siteExternalId || !name) {
        displayAlert(alertError, 'Todos los campos son obligatorios.');
        return;
    }
    const payload = {
        siteExternalId : siteExternalId,
        name: name
    };
    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;
    try {
        const res = await fetchWithAuth('/api/v1/site-zones', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        if (!res || !res.ok) {
            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            if (res){
                const contentType = res.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await res.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            if (createBtn) createBtn.disabled = false;
            if (cancelBtn) cancelBtn.disabled = false;
            return;
        }
        displayAlert(alertSuccess, 'La zona ha sido creada correctamente.', 2000);
        setTimeout(() => {
            if(siteExternalId) {
                navigateTo(`/private/sites/edit/${siteExternalId}`);
            } else {
                navigateTo(`/private/sites/table-view`);
            }
        }, 1500);
    } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        if(createBtn) createBtn.disabled = false;
        if(cancelBtn) cancelBtn.disabled = false;
    }
}

const cancelSiteZone = () => {
    const siteExternalInput = qs('#siteExternalId');
    const siteExternalId = siteExternalInput?.value?.trim() || '';
    displayAlert(alertWarning,
                        'La nueva zona ha sido cancelada', 1500);
    setTimeout(() => {
        if(siteExternalId) {
            navigateTo(`/private/sites/edit/${siteExternalId}`);
        } else {
            navigateTo(`/private/sites/table-view`);
        }
    }, 1500);
}

function bindEvents () {
    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', createSiteZone);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelSiteZone);
    }
}

(function init() {
    bindEvents();
})();