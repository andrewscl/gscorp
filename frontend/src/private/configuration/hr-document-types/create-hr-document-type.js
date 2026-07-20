import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { qs } from "../../../shared/dom-utils";
import { displayAlert } from "../../../shared/display-alert";

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');

const cancelHrDocumentType = () => {
    navigateTo('/private/hr-document-types/list', true);
}

const createHrDocumentType = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');
    const name = qs('#name')?.value?.trim() || '';
    const employeeStatus = qs('#filter-employee-status')?.value || '';
    const hrProcessType = qs('#filter-hr-process')?.value || '';
    if(!name || !employeeStatus || !hrProcessType) {
        displayAlert(alertError, 'Todos los campos son obligatorios.', 2000);
        return;        
    }
    const payload = {
            name: name,
            status: employeeStatus, 
            targetProcess: hrProcessType};
    createBtn.disabled = true;
    cancelBtn.disabled = true;
    try {
        const res = await fetchWithAuth('/api/hr-document-types/create', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(payload)
        });

        if (!res || !res.ok) {
            createBtn.disabled = false;
            cancelBtn.disabled = false;
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

        displayAlert(alertSuccess, 'El tipo de documento ha sido creado correctamente.', 2000);
        setTimeout(() => {
            navigateTo('/private/hr-document-types/list'); // recarga el listado
        }, 2000);

    } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        createBtn.disabled = false;
        cancelBtn.disabled = false;
    }

}

function bindFunctions() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', createHrDocumentType);

    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', cancelHrDocumentType);
}

(function init () {
  bindFunctions();

})();