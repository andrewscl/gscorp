import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

// 🌟 1. Cambiamos a variables simples globales para que el minificador las maneje sin problemas
let currentContainer = null;
let alertSuccess = null;
let alertError = null;
let alertWarning = null;

// 🌟 2. CAMBIO CLAVE: Declaramos 'qs' como una función tradicional. 
// Esto garantiza que esté disponible en CUALQUIER línea del archivo, sin importar el orden.
function qs(selector) {
    if (currentContainer && typeof currentContainer.querySelector === 'function') {
        return currentContainer.querySelector(selector);
    }
    return document.querySelector(selector);
}

function createShiftAssignment() {
    navigateTo('/private/shift-assignments/create', true);
}

async function handleSiteChange(e) {
    const siteExternalId = e.target.value;
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    const employeeSelect = qs('#employeeExternalId');

    console.log("-> Evento change disparado. Sitio seleccionado (UUID):", siteExternalId);

    if (shiftRequestSelect) {
        shiftRequestSelect.innerHTML = '<option value="">Primero seleccione un sitio</option>';
        shiftRequestSelect.disabled = true;
    }
    if (employeeSelect) {
        employeeSelect.disabled = true;
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
    }

    if (!siteExternalId) {
        console.log("-> Sitio vacío seleccionado, deteniendo flujo.");
        return;
    }

    try {
        const url = `/api/shift-requests/sites/${siteExternalId}/requests`;
        console.log("-> Intentando fetch a la URL:", url);
        const response = await fetchWithAuth(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
        });

        if (!response) throw new Error('No se pudieron obtener los turnos del sitio seleccionado.');

        const shiftRequests = await response.json();
        if (shiftRequests.length === 0) {
            if (shiftRequestSelect) shiftRequestSelect.innerHTML = '<option value="">No hay turnos aprobados disponibles</option>';
            return;
        }

        if (shiftRequestSelect) {
            shiftRequestSelect.innerHTML = '<option value="">Seleccione un turno</option>';
            shiftRequests.forEach(sr => {
                const option = document.createElement('option');
                option.value = sr.externalId;
                option.textContent = sr.code;
                shiftRequestSelect.appendChild(option);
            });
            shiftRequestSelect.disabled = false;
        }
    } catch (error) {
        console.error('Error en cascada:', error);
        if (alertError) displayAlert(alertError, 'Ocurrió un error al cargar los turnos del sitio', 3000);
    }
}

function handleShiftChange(e) {
    const employeeSelect = qs('#employeeExternalId');
    if (!employeeSelect) return;

    if (e.target.value) {
        employeeSelect.disabled = false;
        employeeSelect.innerHTML = '<option value="">Seleccione un empleado</option>';
    } else {
        employeeSelect.disabled = true;
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
    }
}

function cancelShiftAssignment() {
    if (alertWarning) displayAlert(alertWarning, 'La asignación de turno ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/shift-assignments/list'), 1500);
}

// 🌟 3. Modificamos la exportación para que el Router inyecte el contenedor de forma segura
export function init({ container }) {
    currentContainer = container;

    console.log("-> [INIT] Inicializando listeners en la vista activa...");

    // Poblamos las alertas en tiempo de ejecución
    alertSuccess = qs('.alert-success');
    alertError = qs('.alert-error');
    alertWarning = qs('.alert-warning');

    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelShiftAssignment);
    }
    
    const siteSelect = qs('#siteExternalId');
    if (siteSelect) {
        siteSelect.addEventListener('change', handleSiteChange);
        console.log("-> Listener 'change' vinculado a #siteExternalId en vista activa.");
    }
    
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    if (shiftRequestSelect) {
        shiftRequestSelect.addEventListener('change', handleShiftChange);
    }
}