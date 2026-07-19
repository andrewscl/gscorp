import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { qs } from "../../../shared/dom-utils";

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

const cancelTerminationRequest = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/employees/transition-requests/list', true);
}

const createTerminationRequest = (btn) => {
    if(btn) btn.disabled = true;

    const employeeExternalId = qs('#employeeExternalId');
    const terminationReason = qs('#terminationReason');
    const proposedExitDate = qs('#proposedExitDate');
    const description = qs('#description');
    const fileUpload = qs('#file-upload');
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

    } catch (e) {
        if(e) e.textContent = e.message;
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