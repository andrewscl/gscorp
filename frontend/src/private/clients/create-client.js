import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');


async function createClient(e) {
    e.preventDefault();

    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const name = qs('#name')?.value?.trim();
    const legalName = qs('#legalName')?.value?.trim();
    const taxId = qs('#taxId')?.value?.trim();
    const email = qs('#email')?.value?.trim();
    const phone = qs('#phone')?.value?.trim();
    const company = qs('#phone')?.value?.trim();

    if(!name || !legalName || !taxId || !company) {
        displayAlert(alertError, 'Los campos obligatorios deben ser completados.', 2000);
        return;        
    }

    const payload = {
        name,
        legalName,
        taxId,
        email,
        phone,
        company
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

        displayAlert(alertSuccess, 'El nuevo cliente ha sido creado correctamente.', 2000);

        setTimeout(() => {
            navigateTo('/private/clients/table-view'); // recarga el listado
        }, 2000);

    } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        createBtn.disabled = false;
        cancelBtn.disabled = false;
    }
}


const cancelCreateClient = () => {
    displayAlert(alertWarning, 'La creación del cliente ha sido cancelada.', 2000);
    setTimeout(() => navigateTo('/private/clients/table-view', true), 2000);
}

function bindEvents() {
    const createBtn = qs('#submit');
    if(createBtn) {
        createBtn.addEventListener('click', createClient);
    }
    const cancelBtn = qs('#cancel');
    if(cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateClient);
    }
}

(async function init() {
  bindEvents();
})();