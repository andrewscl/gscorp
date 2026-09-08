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
    const projectExternalId = qs('#projectExternalId')?.value || '';
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