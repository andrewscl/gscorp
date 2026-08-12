import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { displayAlert } from "../../../shared/display-alert";

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createShift = () => {
    navigateTo('/private/shifts/create', true);
}

const searchShifts = async () => {
    const createBtn = qs('#addShiftsBtn');
    const cancelBtn = qs('#cancel');
    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;

    const from = qs('#filter-from')?.value.trim() || '';
    const to = qs('#filter-to')?.value.trim() || '';
    const siteId = qs('#filter-dept')?.value.trim() || '';
    const url = `/private/shifts/search?from=${from}&to=${to}&siteId=${siteId}`;

    try {
        const res = await fetchWithAuth(url, {credentials: 'same-origin'});
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
        console.error("No se pudo procesar la búsqueda de turnos:", err);
    }
}

function bindEvents() {
    const createBtn = qs('#addShiftsBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShift);
    }
    const searchBtn = qs('#searchshiftsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchShifts);
    }
}

(function init () {
    bindEvents();
})();