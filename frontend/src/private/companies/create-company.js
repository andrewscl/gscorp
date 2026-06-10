import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');

async function createCompany(e) {
    e.preventDefault();

    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');
    const name = qs('#name')?.value?.trim();
    const legalName = qs('#legalName')?.value?.trim();
    const taxId = qs('#taxId')?.value?.trim();

    if(!name || !legalName || !taxId) {
        displayAlert(alertError, 'Todos los campos son obligatorios.', 2000);
        return;        
    }

    const payload = {
        name,
        legalName,
        taxId
    };

    createBtn.disabled = true;
    cancelBtn.disabled = true;

    try {
        const response = await fetchWithAuth('/api/companies/create', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify(payload)
        });

        // Verificar respuesta del servidor
        if (!response.ok) {
            createBtn.disabled = false;
            cancelBtn.disabled = false;

            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            const contentType = response.headers.get('content-type');
            if(contentType && contentType.includes('application/json')) {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }

        displayAlert(alertSuccess, 'La nueva empresa ha sido creada correctamente.', 2000);

        setTimeout(() => {
            navigateTo('/private/companies/table-view'); // recarga el listado
        }, 2000);

    } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        createBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}


const cancelCreateCompany = () => {
    displayAlert(alertWarning, 'La creación de la empresa ha sido cancelada.', 2000);
    setTimeout(() => navigateTo('/private/companies/table-view', true), 2000);
}

function bindEvents() {
    const createBtn = qs('#submit');
    if(createBtn) {
        createBtn.addEventListener('click', createCompany);
    }
    const cancelBtn = qs('#cancel');
    if(cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateCompany);
    }
}

(async function init() {
  bindEvents();
})();