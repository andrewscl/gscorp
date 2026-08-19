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
    if(createBtn) createBtn.disabled = true;

    const from = qs('#filter-from')?.value.trim() || '';
    const to = qs('#filter-to')?.value.trim() || '';
    const siteId = qs('#filter-dept')?.value.trim() || '';
    const shiftRequestExternalId = qs('#filter-shiftRequest')?.value.trim() || '';
    const status = qs('#filter-status')?.value.trim() || '';
    const url = `/private/shifts/search?from=${from}&to=${to}&siteId=${siteId}&status=${status}&shiftRequestExternalId=${shiftRequestExternalId}`;

    try {
        const res = await fetchWithAuth(url, {credentials: 'same-origin'});
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
        console.error("No se pudo procesar la búsqueda de turnos:", err);
    }
}

const loadShiftRequestsBySite = async () => {
    const siteExternalId = qs('#filter-dept')?.value.trim() || '';
    if (!siteExternalId) {
        filterShiftRequest.innerHTML = '<option value="">Todos los turnos</option>';
        filterShiftRequest.disabled = true;
        return;
    }
    const filterShiftRequest = qs('#filter-shiftRequest');
    if (!filterShiftRequest) return;
    try {
        filterShiftRequest.innerHTML = '<option value="">Cargando turnos...</option>';
        filterShiftRequest.disabled = true;
        const url = `/api/shift-requests/sites/${siteExternalId}/requests`;
        const response = await fetchWithAuth(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        if (!response || !response.ok) {
            throw new Error('No se pudieron obtener los turnos del sitio seleccionado.');
        }
        const shiftRequests = await response.json();
        if (shiftRequests.length === 0) {
            filterShiftRequest.innerHTML = '<option value="">No hay turnos disponibles</option>';
            filterShiftRequest.disabled = true;
            return;
        }
        filterShiftRequest.innerHTML = '<option value="">Todos los turnos</option>';
        shiftRequests.forEach(sr => {
            const option = document.createElement('option');
            option.value = sr.externalId;
            let textoHorarios = 'Sin horario';
            if (sr.schedules && sr.schedules.length > 0) {
                textoHorarios = sr.schedules.map(sch => {
                    const inicio = sch.startTime ? sch.startTime.substring(0, 5) : '??:??';
                    const fin = sch.endTime ? sch.endTime.substring(0, 5) : '??:??';
                    const desde = sch.dayFrom ? sch.dayFrom.substring(0, 3) : '';
                    const hasta = sch.dayTo ? sch.dayTo.substring(0, 3) : '';
                    if (desde === hasta || !hasta) {
                        return `${desde} ${inicio}-${fin}`;
                    }
                    return `${desde} a ${hasta} ${inicio}-${fin}`;
                }).join(' | ');
            }
            option.textContent = `${sr.code} - [${textoHorarios}]`;
            filterShiftRequest.appendChild(option);
        });
        filterShiftRequest.disabled = false;
    } catch (error) {
        console.error('Error al cargar turnos del sitio:', error);
        filterShiftRequest.innerHTML = '<option value="">Error al cargar turnos</option>';
        filterShiftRequest.disabled = true;
        displayAlert(alertError, 'Ocurrió un error al cargar los turnos del sitio.', 3000);
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
    const siteSelect = qs('#filter-dept');
    if (siteSelect) {
        siteSelect.addEventListener('change', loadShiftRequestsBySite);
    }
}

(function init () {
    bindEvents();
})();