import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { qs } from "../../../shared/dom-utils";
import { displayAlert } from "../../../shared/display-alert";

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const cancelTerminationRequest = () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');
    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;
    displayAlert(alertCancel, 'La gestión de desvinculación ha sido cancelada.', 1500);
    setTimeout(() => {
        navigateTo('/private/employee-terminations/list');
    }, 1500);
}

const processManageTerminationRequest = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const externalId = qs('#externalId')?.value || '';
    const employeeExternalId = qs('#employeeExternalId')?.value || '';
    const finalTerminationReason = qs('#finalTerminationReason')?.value || '';
    const finalExitDate = qs('#finalExitDate')?.value || '';
    const hrDocumentType = qs('#hrDocumentType')?.value || '';

    const fileInput = qs('#file-upload');    
    const fileUpload = fileInput?.files ? fileInput.files[0] : null;

    if (!externalId || !employeeExternalId || !finalTerminationReason || !finalExitDate || !hrDocumentType || !fileUpload) {
        displayAlert(alertError, 'Por favor, complete todos los campos y adjunte el documento obligatorio.');
        return;
    }

    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;

    try {
        const formData = new FormData();
        if(externalId) formData.append('externalId', externalId);
        if(employeeExternalId) formData.append('employeeId', employeeExternalId);
        if(finalTerminationReason) formData.append('finalTerminationReason', finalTerminationReason);
        if(finalExitDate) formData.append('finalExitDate', finalExitDate);
        if(hrDocumentType) formData.append('hrDocumentType', hrDocumentType);
        if(fileUpload) formData.append('file', fileUpload);

        const res = await fetchWithAuth('/api/employee-terminations/manage', {
        method: 'POST',
        body: formData
        });

        if (!res || !res.ok) {
            if(createBtn) createBtn.disabled = false;
            if(cancelBtn) cancelBtn.disabled = false;
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

        displayAlert(alertSuccess, 'La solicitud de desvinculación ha sido gestionada correctamente.', 2000);
        setTimeout(() => {
            navigateTo('/private/employee-terminations/list');
        }, 2000);

    } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        if(createBtn) createBtn.disabled = false;
        if(cancelBtn) cancelBtn.disabled = false;
    }

}

function listenFileUpload() {
    const fileInput = qs('#file-upload');
    const fileNameSpan = qs('#selected-file-name');

    if (fileInput && fileNameSpan) {
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                // Cambia el texto gris por el nombre real del archivo
                fileNameSpan.textContent = file.name;
                fileNameSpan.style.color = "#4f46e5"; // Un toque de color azul/morado opcional para resaltar
            } else {
                fileNameSpan.textContent = 'Ningún archivo seleccionado';
                fileNameSpan.style.color = "";
            }
        });
    }
}

function bindFunctions() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', processManageTerminationRequest);

    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', cancelTerminationRequest);
}

(function init () {
    bindFunctions();
    listenFileUpload();
})();