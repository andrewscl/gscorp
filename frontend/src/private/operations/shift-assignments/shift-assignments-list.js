import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createShiftAssignment = () => {
    navigateTo('/private/shift-assignments/create', true);
}

const searchShiftAssignments = async () => {
    const createBtn = qs('#addShiftAssignmentsBtn')
    if(createBtn) createBtn.disabled = true;

    const siteExternalId = qs('#filter-dept')?.value.trim() || '';
    const url = `/private/shift-assignments/search?siteExternalId=${siteExternalId}&status=${status}`;

    try {
        const res = await fetchWithAuth(url, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        if (!res || !res.ok) {
            if(createBtn) createBtn.disabled = false;
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
        const htmlResult = await res.text();
        const tBody = qs('.hs-table-container .table tbody');
        if(tBody){
            tBody.innerHTML = htmlResult;
        }
        const hiddenCountInput = qs('#sync-shifts-count');
        const headerCountSpan = qs('.count');
        if(hiddenCountInput && headerCountSpan){
            const newCount = parseInt(hiddenCountInput.value, 10) || 0;
            headerCountSpan.textContent = `${newCount} registro${newCount === 1 ? '' : 's'}`;
        }
    } catch (err) {
        console.error("No se pudo procesar la busqueda de asignaciones de turno.", err);
    }
}

function bindShiftAssignmentsList() {
    const createBtn = qs('#addShiftAssignmentsBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const searchBtn = qs('#searchShiftAssignmentsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchShiftAssignments);
    }
}

(function init () {
  bindShiftAssignmentsList();

})();