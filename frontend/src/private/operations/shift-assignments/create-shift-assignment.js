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

async function handleProjectChange() {
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    const employeeSelect = qs('#employeeExternalId');
    const siteSelect = qs('#siteExternalId');
    const projectExternalId = qs('#projectExternalId')?.value;
    const shiftPatternGroup = qs('#shiftPatternGroup');
    const shiftPatternSpan = qs('#shiftPattern');

    if (siteSelect) {
        siteSelect.innerHTML = '<option value="">Primero seleccione un proyecto</option>';
        siteSelect.disabled = true;
    }
    if (shiftRequestSelect) {
        shiftRequestSelect.innerHTML = '<option value="">Primero seleccione un sitio</option>';
        shiftRequestSelect.disabled = true;
    }
    if (shiftPatternGroup) {
        shiftPatternGroup.style.display = 'none';
    }
    if (shiftPatternSpan) {
        shiftPatternSpan.textContent = '-';
    }
    if (employeeSelect) {
        employeeSelect.innerHTML = '<option value="">Primero seleccione un turno</option>';
        employeeSelect.disabled = true;
    }
    if(!projectExternalId) return;
    try {
        const urlSites = `/api/sites/projects/${projectExternalId}/sites`;
        const urlEmployees = `/api/employees/projects/${projectExternalId}/employees`;
        const [siteResponse, employeeResponse] = await Promise.all([
            fetchWithAuth(urlSites, {method: 'GET', headers: { 'Accept': 'application/json' },}),
            fetchWithAuth(urlEmployees, {method: 'GET', headers: { 'Accept': 'application/json' },})
        ]);
        if(!siteResponse) throw new Error('No se pudieron obtener los sitios del proyecto seleccionado.');
        if(!employeeResponse) throw new Error('No se pudieron obtener los empleados del proyecto seleccionado.');
        const sitesProject = await siteResponse.json();
        if (sitesProject.length === 0) {
            siteSelect.innerHTML = '<option value="">No hay sitios disponibles para el proyecto seleccionado</option>';
        } else {
            siteSelect.innerHTML = '<option value="">Seleccione un sitio</option>';
            sitesProject.forEach(site => {
                const option = document.createElement('option');
                option.value = site.externalId;
                option.textContent = site.name;
                siteSelect.appendChild(option);
            });
            siteSelect.disabled = false;
        }
        const employeesProject = await employeeResponse.json();
        if (employeesProject.length === 0) {
            employeeSelect.innerHTML = '<option value="">No hay empleados disponibles para el proyecto seleccionado</option>';
        } else {
            employeeSelect.innerHTML = '<option value="">Seleccione un empleado</option>';
            employeesProject.forEach(employee => {
                const option = document.createElement('option');
                option.value = employee.externalId;
                option.textContent = employee.fullName;
                employeeSelect.appendChild(option);
            });
            employeeSelect.disabled = false;
        }
    } catch (error) {
        console.error('Error en cascada:', error);
        displayAlert(alertError, 'Ocurrió un error al cargar los sitios y/o empleados del proyecto', 3000);
    }
}

async function handleSiteChange() {
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    const shiftPatternGroup = qs('#shiftPatternGroup');
    const shiftPatternSpan = qs('#shiftPattern');
    const employeeSelect = qs('#employeeExternalId');
    const siteExternalId = qs('#siteExternalId')?.value;

    if (shiftRequestSelect) {
        shiftRequestSelect.innerHTML = '<option value="">Primero seleccione un sitio</option>';
        shiftRequestSelect.disabled = true;
    }
    if (shiftPatternGroup) {
        shiftPatternGroup.style.display = 'none';
    }
    if (shiftPatternSpan) {
        shiftPatternSpan.textContent = '-';
    }
    if (employeeSelect) {
        employeeSelect.value = '';
        employeeSelect.disabled = true;
    }
    if(!siteExternalId) return;
    try {
        const url = `/api/shift-requests/sites/${siteExternalId}/requests`
        const response = await fetchWithAuth(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        });
        if(!response) throw new Error('No se pudieron obtener los turnos del sitio seleccionado.');
        const shiftRequests = await response.json();
        if (shiftRequests.length === 0) {
            shiftRequestSelect.innerHTML = '<option value="">No hay turnos aprobados disponibles</option>';
            return;
        }
        if (shiftRequestSelect) {
            shiftRequestSelect.innerHTML = '<option value="">Seleccione un turno</option>';
            
            // 'requests' es la lista List<ShiftRequestSelectDto> que te devolvió el controlador
            shiftRequests.forEach(sr => {
                const option = document.createElement('option');
                option.value = sr.externalId; // Lo que lee la máquina (el UUID)
                let textoHorarios = 'Sin horario cargado';
                // Formateamos y unimos los horarios del turno
                if (sr.schedules && sr.schedules.length > 0) {
                    textoHorarios = sr.schedules.map(sch => {
                        const inicio = sch.startTime ? sch.startTime.substring(0, 5) : '??:??';
                        const fin = sch.endTime ? sch.endTime.substring(0, 5) : '??:??';
                        const desde = sch.dayFrom ? sch.dayFrom.substring(0, 3) : '';
                        const hasta = sch.dayTo ? sch.dayTo.substring(0, 3) : '';
                        
                        // Si el horario es del mismo día (ej: LUN a LUN)
                        if (desde === hasta || !hasta) {
                            return `${desde} ${inicio}-${fin}`;
                        }
                        // Si abarca un rango (ej: LUN a VIE)
                        return `${desde} a ${hasta} ${inicio}-${fin}`;
                    }).join(' | '); // Si hay más de un horario en el mismo turno, los separa con una barra
                }
                
                // 🌟 Se renderiza una ÚNICA opción por turno con sus horarios al lado
                // Ejemplo visual: "TURNO-A - [LUN a VIE 08:00-16:00]"
                option.textContent = `${sr.code} - [${textoHorarios}]`;
                shiftRequestSelect.appendChild(option);
            });
            
            shiftRequestSelect.disabled = false;
        }

    } catch (error) {
        console.error('Error en cascada:', error);
        displayAlert(alertError, 'Ocurrió un error al cargar los turnos del sitio', 3000);
    }

}

async function handleShiftChange (e) {
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    const shiftPatternGroup = qs('#shiftPatternGroup');
    const employeeSelect = qs('#employeeExternalId');
    const shiftPatternSpan = qs('#shiftPattern');
    const shiftRequestExternalId = e.target.value;

    const resetUIOnError = () => {
        if(shiftPatternSpan) shiftPatternSpan.textContent = '-';
        if(shiftPatternGroup) shiftPatternGroup.style.display = 'none';
        if(employeeSelect) {
            employeeSelect.value = '';
            employeeSelect.disabled = true;
        }
    };
    if (!shiftRequestExternalId) {
        resetUIOnError();
        return
    }
    try {
        const urlShiftRequest = `/api/shift-requests/${shiftRequestExternalId}`;
        const response = await fetchWithAuth(urlShiftRequest, { method: 'GET', headers: { 'Accept': 'application/json' },
                                                            });
        if (!response || !response.ok) {
            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            if(response){
                const contentType = response.headers.get('content-type');
                if(contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            resetUIOnError();
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }
        const shiftRequest = await response.json();
        
        if (shiftPatternSpan){
            shiftPatternSpan.textContent = shiftRequest.shiftPatternName || '-';
        }
        if(shiftPatternGroup){
            shiftPatternGroup.style.display = '';
        }
        if(employeeSelect) {
            employeeSelect.disabled = false;
            employeeSelect.value = '';
        }
    } catch (error) {
        console.error('Error en cascada:', error);
        resetUIOnError();
        displayAlert(alertError, 'Ocurrió un error al cargar los sitios y/o empleados del proyecto', 3000);
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
    const projectSelect = qs('#projectExternalId');
    if (projectSelect) {
        projectSelect.addEventListener('change', handleProjectChange);
    }
    const siteSelect = qs('#siteExternalId');
    if (siteSelect) {
        siteSelect.addEventListener('change', handleSiteChange);
    }
    const shiftRequestSelect = qs('#shiftRequestExternalId');
    if (shiftRequestSelect) {
        shiftRequestSelect.addEventListener('change', handleShiftChange);
    }
}

(function init() {
    bindEvents();
})();