import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createProject = () => {
}

async function handleCompanyChange () {
    const companyExternalId = qs('#company')?.value;
    const clientSelect = qs('#client');

    if (clientSelect) {
        clientSelect.innerHTML = '<option value="">Primero seleccione una empresa</option>';
        clientSelect.disabled = true;
    }

    try {
            const urlClients = `/api/clients/company/${companyExternalId}/clients?status=ACTIVE`;
            const response = await fetchWithAuth(urlClients, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
            });
            if (!response || !response.ok) {
                let errorMessage = 'Ocurrió un problema al obtener los clientes para la empresa seleccionada.';
                if(response){
                    const contentType = response.headers.get('content-type');
                    if(contentType && contentType.includes('application/json')) {
                        const errorData = await response.json();
                        errorMessage = errorData.message || errorMessage;
                    }
                }
                displayAlert(alertError, `Error: ${errorMessage}`);
                return;
            }
            const clientsData = await response.json();
            if (clientSelect) {
                clientSelect.innerHTML = '<option value="">Seleccione un cliente</option>';
                clientsData.forEach(client => {
                    const option = document.createElement('option');
                    option.value = client.externalId;
                    option.textContent = client.name;
                    clientSelect.appendChild(option);
                });
                clientSelect.disabled = false;
            }
    } catch (error) {
        console.error('Error en cascada:', error);
        resetUIOnError();
        displayAlert(alertError, 'Ocurrió un error al cargar los clientes', 3000);
    }


}

const cancelCreateProject = () => {
    displayAlert(alertWarning,
                'La creación del proyecto ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/projects/list'), 1500);
}

function bindEvents () {
    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', createProject);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateProject);
    }
    const companySelect = qs('#company');
    if (companySelect) {
        companySelect.addEventListener('change', handleCompanyChange);
    }
}

(function init() {
    bindEvents();
})();