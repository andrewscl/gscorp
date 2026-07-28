import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const siteSelect = qs('#siteExternalId');
const shiftRequestSelect = qs('#shiftRequestExternalId');
const employeeSelect = qs('#employeeExternalId');

const createShiftAssignment = () => {
    navigateTo('/private/shift-assignments/create', true);
}

async function handleSiteChange(e) {
    const siteExternalId = e.target.value;

    // Resetear selectores hijos por defecto
    shiftRequestSelect.innerHTML = '<option value="">Primero seleccione un sitio</option>';
    shiftRequestSelect.disabled = true;
    if(employeeSelect) {
        employeeSelect.disabled = true;
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
    }
    if(!siteExternalId) return;

    try {
        const response = await fetchWithAuth(`/api/shift-requests/sites/${siteExternalId}/requests`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        });

        if(!response) throw new Error('No se pudieron obtener los turnos del sitio seleccionado.');

        const shiftRequests = await response.json();
        if (shiftRequests.length === 0) {
            shiftSelect.innerHTML = '<option value="">No hay turnos aprobados disponibles</option>';
            return;
        }

        // Poblar las opciones del selector de Turnos
        shiftSelect.innerHTML = '<option value="">Seleccione un turno</option>';
        shiftRequests.forEach(sr => {
            const option = document.createElement('option');
            option.value = sr.externalId;
            option.textContent = sr.code;
            shiftSelect.appendChild(option);
        });

        shiftSelect.disabled = false;
    } catch {
        console.error('Error en cascada:', error);
        displayAlert(alertError, 'Ocurrió un error al cargar los turnos del sitio', 3000);
    }

}

function handleShiftChange (e) {
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

function bindCreateShiftAssignments() {
    const createBtn = qs('#createShiftAssignmentBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const searchBtn = qs('#cancelShiftAssignmentBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', cancelShiftAssignment);
    }
    // Flujo dinamico
    if (siteSelect) siteSelect.addEventListener('change', );
}

(function init () {
  bindCreateShiftAssignments();

})();