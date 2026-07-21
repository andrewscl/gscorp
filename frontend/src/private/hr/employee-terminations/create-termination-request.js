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
    displayAlert(alertCancel, 'La solicitud de desvinculación ha sido cancelada.', 1500);
    setTimeout(() => {
        navigateTo('/private/employee-terminations/list');
    }, 1500);
}

const createTerminationRequest = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const employeeExternalId = qs('#employeeExternalId')?.value || '';
    const terminationReason = qs('#terminationReason')?.value || '';
    const proposedExitDate = qs('#proposedExitDate')?.value || '';
    const description = qs('#description')?.value || '';

    const fileInput = qs('#file-upload');    
    const fileUpload = fileInput?.files ? fileInput.files[0] : null;

    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;

    try {
        const formData = new FormData();
        if(employeeExternalId) formData.append('employeeExternalId', employeeExternalId);
        if(terminationReason) formData.append('terminationReason', terminationReason);
        if(proposedExitDate) formData.append('proposedExitDate', proposedExitDate);
        if(description) formData.append('description', description);
        if(fileUpload) formData.append('fileUpload', fileUpload);

        const res = await fetchWithAuth('/api/employee-terminations/create', {
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

        displayAlert(alertSuccess, 'La solicitud de desvinculación ha sido creada correctamente.', 2000);
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

function bindFunctions() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', createTerminationRequest);

    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', cancelTerminationRequest);
}

(function init () {
  bindFunctions();

})();