import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createShiftAssignment = () => {
    navigateTo('/private/shift-assignments/create', true);
}

async function handleSiteChange() {
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    const employeeSelect = qs('#employeeExternalId');
    const siteExternalId = qs('#siteExternalId')?.value;

    console.log("-> Elemento encontrado:", qs('#siteExternalId'));
    console.log("-> Evento change disparado. Sitio seleccionado (UUID):", siteExternalId);

    // Resetear selectores hijos por defecto
    shiftRequestSelect.innerHTML = '<option value="">Primero seleccione un sitio</option>';
    shiftRequestSelect.disabled = true;
    if(employeeSelect) {
        employeeSelect.disabled = true;
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
    }
    if(!siteExternalId) {
        console.log("-> Sitio vacío seleccionado, deteniendo flujo.");
        return;
    }

    try {
        const url = `/api/shift-requests/sites/${siteExternalId}/requests`
        console.log("-> Intentando fetch a la URL:", url);
        const response = await fetchWithAuth(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        });

        if(!response) throw new Error('No se pudieron obtener los turnos del sitio seleccionado.');

        const shiftRequests = await response.json();
        if (shiftRequests.length === 0) {
            shiftRequestSelect.innerHTML = '<option value="">No hay turnos aprobados disponibles</option>';
            console.log("-> La lista de turnos volvió vacía (0 elementos).");
            return;
        }

        // Poblar las opciones del selector de Turnos
        shiftRequestSelect.innerHTML = '<option value="">Seleccione un turno</option>';
        shiftRequests.forEach(sr => {
            const option = document.createElement('option');
            option.value = sr.externalId;
            option.textContent = sr.code;
            shiftRequestSelect.appendChild(option);
        });
        shiftRequestSelect.disabled = false;
        console.log("-> Selector de turnos poblado y habilitado con éxito.");
    } catch (error) {
        console.error('Error en cascada:', error);
        displayAlert(alertError, 'Ocurrió un error al cargar los turnos del sitio', 3000);
    }

}

function handleShiftChange (e) {
    const employeeSelect = qs('#employeeExternalId');
    if(e.target.value) {
        employeeSelect.disabled = false;
        employeeSelect.innerHTML = '<option value="">Seleccione un empleado</option>';
    } else {
        employeeSelect.disabled = true;
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
    }
}

const cancelShiftAssignment = () => {
    displayAlert(alertWarning,
                'La asignación de turno ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/shift-assignments/list'), 1500);
}

function bindEvents () {
    const createBtn = qs('#submita');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelShiftAssignment);
    }
    const siteSelect = qs('#submit');
    if (siteSelect) {
        siteSelect.addEventListener('click', handleSiteChange);
    }
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    if (shiftRequestSelect) {
        shiftRequestSelect.addEventListener('click', handleShiftChange);
    }
}

(function init() {
    bindEvents();
})();