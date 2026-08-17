import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const createShiftAssignment = async () => {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const projectExternalId = qs('#projectExternalId')?.value || '';
    const siteExternalId = qs('#siteExternalId')?.value || '';
    const shiftRequestExternalId = qs('#shiftRequestExternalId')?.value || '';
    const employeeExternalId = qs('#employeeExternalId')?.value || ''
    const shiftPatternStartCycle = qs('#shiftPatternStartCycle')?.value || '';
    const description = qs('#description')?.value || '';
    const assignmentStartDateInput = qs('#assignmentStartDate');
    const assignmentStartDate = assignmentStartDateInput?.value || '';
    const assignmentEndDate = qs('#assignmentEndDate')?.value || '';
    const minAvailableShiftDateStr = assignmentStartDateInput?.dataset.nextAvailableShift;
    console.log(`[onClickCreate] minAvailableShiftDateStr: ${minAvailableShiftDateStr}`);
    if (!projectExternalId || !siteExternalId || 
        !shiftRequestExternalId || !employeeExternalId || 
        !shiftPatternStartCycle || !assignmentStartDate
    ){
        displayAlert(alertError, 'Por favor, complete los campos minimos requeridos.');
        return;
    }
    // Convertir fechas a formato ISO-8601 válido para OffsetDateTime
    const startObj = new Date(assignmentStartDate);
    const endObj = assignmentEndDate ? new Date(assignmentEndDate) : null;
    if (isNaN(startObj.getTime()) || (assignmentEndDate && isNaN(endObj.getTime()))) {
        displayAlert(alertError, 'Por favor, ingrese fechas válidas.');
        return;
    }
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    if (assignmentStartDate < todayStr) {
        displayAlert(alertError, 'La fecha de inicio no puede ser anterior a la fecha actual.');
        return;
    }
    if (minAvailableShiftDateStr && assignmentStartDate < minAvailableShiftDateStr ) {
        displayAlert(alertError, `La fecha de inicio no puede ser anterior a: ${minAvailableShiftDateStr}`);
        return;
    }
    if (endObj && endObj < startObj) {
        displayAlert(alertError, 'La fecha de fin no puede ser anterior a la fecha de inicio.');
        return;
    }

    const assignedAtIso = startObj.toISOString();
    const assignedUntilIso = endObj ? endObj.toISOString() : null;
    const payload = {
        siteExternalId: siteExternalId,
        shiftRequestExternalId: shiftRequestExternalId,
        employeeExternalId: employeeExternalId,
        startCycleNumber: parseInt(shiftPatternStartCycle, 10),
        notes: description.trim() || null,
        assignedAt: assignedAtIso,
        assignedUntil: assignedUntilIso
    }
    if(createBtn) createBtn.disabled = true;
    if(cancelBtn) cancelBtn.disabled = true;

    try {
            const res = await fetchWithAuth('/api/shift-assignments', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Time-Zone': Intl.DateTimeFormat().resolvedOptions().timeZone
                },
                body: JSON.stringify(payload)
            });
            if (!res || !res.ok) {
                let errorMessage = 'Ocurrió un problema al enviar el formulario.';
                if(res){
                    const contentType = res.headers.get('content-type');
                    if(contentType && contentType.includes('application/json')) {
                        const errorData = await res.json();
                        errorMessage = errorData.message || errorMessage;
                    }
                }
                displayAlert(alertError, `Error: ${errorMessage}`);
                if(createBtn) createBtn.disabled = false;
                if(cancelBtn) cancelBtn.disabled = false;
                return;
            }
            displayAlert(alertSuccess, 'La asignación de turno ha sido creada correctamente.', 2000);
            setTimeout(() => {
                navigateTo('/private/shift-assignments/list', true);
            }, 2000);

    } catch (error) {
            console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
            displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
            if(createBtn) createBtn.disabled = false;
            if(cancelBtn) cancelBtn.disabled = false;
    }
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
    const shiftRequestTypeGroup = qs('#shiftRequestTypeGroup');
    const shiftRequestTypeSpan = qs('#shiftRequestType');
    const shiftPatternGroup = qs('#shiftPatternGroup');
    const shiftPatternSpan = qs('#shiftPattern');
    const employeeSelect = qs('#employeeExternalId');
    const assignmentStartDateInput = qs('#assignmentStartDate')
    const assignmentEndDateGroup = qs('#assignmentEndDateGroup');
    const assignmentEndDateInput = qs('#assignmentEndDate');
    const cycleSelect = qs('#shiftPatternStartCycle');
    const shiftRequestExternalId = e.target.value;

    const resetUIOnError = () => {
        if(shiftRequestTypeSpan) shiftRequestTypeSpan.textContent = '-';
        if(shiftRequestTypeGroup) shiftRequestTypeGroup.style.display = 'none';
        if(shiftPatternSpan) shiftPatternSpan.textContent = '-';
        if(shiftPatternGroup) shiftPatternGroup.style.display = 'none';
        if(assignmentEndDateGroup) assignmentEndDateGroup.style.display = 'none';
        if(assignmentEndDateInput) {
            assignmentEndDateInput.value = '';
            assignmentEndDateInput.required = false;
        }
        if(assignmentStartDateInput) {
            assignmentStartDateInput.removeAttribute('min');
            delete assignmentStartDateInput.dataset.nextAvailableShift;
        }
        if(employeeSelect) {
            employeeSelect.value = '';
            employeeSelect.disabled = true;
        }
        if(cycleSelect){
            cycleSelect.innerHTML = '<option value=""></option>';
            cycleSelect.disabled = true;
        }
    };
    if (!shiftRequestExternalId) {
        resetUIOnError();
        return
    }
    try {
        const urlShiftRequest = `/api/shift-requests/${shiftRequestExternalId}`;
        const response = await fetchWithAuth(urlShiftRequest, { 
                                method: 'GET', 
                                headers: { 'Accept': 'application/json' },
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

        const typeObj = shiftRequest.type || {};
        const typeDisplay = typeObj.displayName || '-';
        const typeName = typeObj.name;

        if(shiftRequestTypeSpan) shiftRequestTypeSpan.textContent = typeDisplay;
        if(shiftRequestTypeGroup) shiftRequestTypeGroup.style.display = '';
        const isSporadic = typeName == 'SPORADIC';
        if(assignmentEndDateGroup) {
            if(isSporadic) {
                assignmentEndDateGroup.style.display = '';
                if(assignmentEndDateInput) assignmentEndDateInput.required = true;
            } else {
                assignmentEndDateGroup.style.display = 'none';
                if(assignmentEndDateInput) {
                    assignmentEndDateInput.value = '';
                    assignmentEndDateInput.required = false;
                }
            }
        }
        updateCycleStartOptions(shiftRequest.cycleDaysList);
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
        // Proximo turno disponible
        const currentDateStr =
            assignmentStartDateInput?.value || new Date().toISOString().split('T')[0];
        const urlNextShift = `/api/shifts/next-available-shift/${shiftRequestExternalId}/shift?startAssignmentDate=${currentDateStr}`;
        const nextShiftResponse = await fetchWithAuth(urlNextShift, { 
                                method: 'GET', 
                                headers: { 'Accept': 'application/json' },
                            });
        if (nextShiftResponse && nextShiftResponse.status === 200) {
            const nextShift = await nextShiftResponse.json();
            if (nextShift && nextShift.shiftDate && assignmentStartDateInput) {
                // Restringe el picker HTML5 para no seleccionar fechas pasadas al turno
                assignmentStartDateInput.min = nextShift.shiftDate;
                assignmentStartDateInput.dataset.nextAvailableShift = nextShift.shiftDate;
                // Si no hay fecha seleccionada o la actual es anterior a la disponible, la actualiza
                if (!assignmentStartDateInput.value || assignmentStartDateInput.value < nextShift.shiftDate) {
                    assignmentStartDateInput.value = nextShift.shiftDate;
                }
            }
        }
    } catch (error) {
        console.error('Error en cascada:', error);
        resetUIOnError();
        displayAlert(alertError, 'Ocurrió un error al cargar la información de los requerimientos', 3000);
    }
}

function updateCycleStartOptions(cycleDaysList) {
    const cycleSelect = qs('#shiftPatternStartCycle');
    if (!cycleSelect) return;

    // Limpiar opciones previas
    cycleSelect.innerHTML = '<option value="">Seleccione día</option>';

    if (Array.isArray(cycleDaysList) && cycleDaysList.length > 0) {
        cycleDaysList.forEach(day => {
            const option = document.createElement('option');
            option.value = day;
            option.textContent = `Día ${day}`;
            cycleSelect.appendChild(option);
        });
        cycleSelect.disabled = false;
    } else {
        cycleSelect.disabled = true;
    }
}

const cancelShiftAssignment = () => {
    displayAlert(alertWarning,
                'La asignación de turno ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/shift-assignments/list'), 1500);
}

function bindEvents () {
    const createBtn = qs('#submit');
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