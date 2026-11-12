import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createProject = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const companyExternalId = qs('#company')?.value;
    const clientExternalId = qs('#client')?.value;
    const name = qs('#name')?.value;
    const startDate = qs('#startDate')?.value;
    const endDate = qs('#endDate')?.value;
    const description = qs('#description')?.value;

    if (!companyExternalId || !clientExternalId || !name || !startDate) {
        displayAlert(alertError, 'Por favor complete todos los campos obligatorios', 3000);
        return;
    }

    const payload = {
        companyExternalId,
        clientExternalId,
        name,
        startDate,
        endDate,
        description
    };

    createBtn.disabled = true;
    cancelBtn.disabled = true;

    try {
        const response = await fetchWithAuth('/api/projects/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response || !response.ok) {
            let errorMessage = 'Ocurrió un problema al crear el proyecto.';
            if (response) {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }
        const projectData = await response.json();
        displayAlert(alertSuccess, 'Proyecto creado exitosamente', 3000);
        setTimeout(() => navigateTo('/private/projects/list'), 3000);
    } catch (error) {
        console.error('Error al crear el proyecto:', error);
        displayAlert(alertError, 'Ocurrió un error al crear el proyecto', 3000);
    } finally {
        createBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}

async function handleCompanyChange () {
    const companyExternalId = qs('#company')?.value;
    const clientSelect = qs('#client');

    if (!clientSelect) return;
    if (!companyExternalId) return;

    if (clientSelect) {
        clientSelect.innerHTML = '<option value="">Primero seleccione una empresa</option>';
        clientSelect.disabled = true;
    }

    try {
        const urlClients = `/api/clients/company/${encodeURIComponent(companyExternalId)}/clients`;
        const response = await fetchWithAuth(urlClients, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
        });
        if (!response || !response.ok) {
            let errorMessage = 'Ocurrió un problema al obtener los clientes para la empresa seleccionada.';
            if (response){
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            clientSelect.innerHTML = '<option value="">Error al cargar clientes</option>';
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
        console.error('Error al obtener los clientes:', error);
        displayAlert(alertError, 'Ocurrió un error al obtener los clientes', 3000);
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