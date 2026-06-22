import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { onClickAddTimeSchedule,
        onClickRemoveItem
        } from './create-patrol.js';

const qs  = (s) => document.querySelector(s);
const qsa = (s) => document.querySelectorAll(s);

async function handleUpdate(e) {
    e.preventDefault();

    // Recolectar schedules con su estado activo/inactivo
        const schedules = Array.from(qsa('.schedule')).map(container => {
        const timeInput = container.querySelector('input[name="scheduleTime[]"]');
        const statusSpan = container.querySelector('.status-text');

        if(timeInput) {
            return {
                startTime: timeInput.value,
                // LÓGICA DE PROTECCIÓN:
                // Si 'statusSpan' existe (registro viejo), leemos su texto.
                // Si 'statusSpan' es null (registro nuevo), asignamos true automáticamente.
                active: statusSpan ? (statusSpan.innerText.trim() === "Activo") : true
            };
        }
        return null;

        }).filter(container => container !== null && container.startTime !== "");

    // Ejecutar sincronización y guardar el resultado
    const consolidatedCheckpoints = syncCheckpoints();
    console.log("Checkpoints a enviar:", JSON.stringify(consolidatedCheckpoints, null, 2));

    const externalId = qs('#patrolExternalId')?.value; // ID de la ronda a actualizar
    // Alertas
    const alertSuccess = qs('.alert-success');
    const alertError = qs('.alert-error');

    // 1. Recolección de datos (Payload)
    const payload = {
        siteId: parseInt(qs('#siteId').value),
        name: qs('#patrolName').value.trim(),
        description: qs('#patrolDescription').value.trim(),
        dayFrom: parseInt(qs('#dayFrom').value),
        dayTo: parseInt(qs('#dayTo').value),
        active: true,
        schedules: schedules,
        checkpoints: consolidatedCheckpoints
    };

    // 2. Validación básica
    if (!payload.name) {
        displayAlert(alertError,
                        "El nombre del punto de control es obligatorio.");
        return;
    }

    // 3. Envío al Servidor
    try {
        const response = await fetchWithAuth(`/api/patrols/update/${externalId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            displayAlert(alertSuccess, "Ronda actualizada correctamente.");
            setTimeout(() => {
                navigateTo('/private/patrols/table-view'); // Redirigir a la tabla
            }, 1500);
            localStorage.removeItem('pending_checkpoints');
        } else {
            const errorData = await response.json();
            displayAlert(alertError
                    , errorData.message || "Error al actualizar la ronda.");
        }
    } catch (error) {
        console.error("Error:", error);
        displayAlert(alertError, "Error de conexión con el servidor.");
    }
}

function toggleSchedule(button) {
    const container = button.closest('.schedule-item');
    const statusText = button.querySelector('.status-text');
    const isCurrentlyActive = container.getAttribute('data-active') === 'true';

    if (isCurrentlyActive) {
        // Pasar a INACTIVO
        container.setAttribute('data-active', 'false');
        container.style.opacity = "0.5"; // Feedback visual
        button.classList.replace('btn-outline-success', 'btn-outline-secondary');
        statusText.innerText = 'Inactivo';
    } else {
        // Pasar a ACTIVO
        container.setAttribute('data-active', 'true');
        container.style.opacity = "1";
        button.classList.replace('btn-outline-secondary', 'btn-outline-success');
        statusText.innerText = 'Activo';
    }
}

function toggleCheckpoint(button) {
    const container = button.closest('.checkpoint-item');
    const statusText = button.querySelector('.status-text');
    const isCurrentlyActive = container.getAttribute('data-active') === 'true';

    if (isCurrentlyActive) {
        // Pasar a INACTIVO
        container.setAttribute('data-active', 'false');
        container.style.opacity = "0.5"; // Feedback visual
        button.classList.replace('btn-outline-success', 'btn-outline-secondary');
        statusText.innerText = 'Inactivo';
    } else {
        // Pasar a ACTIVO
        container.setAttribute('data-active', 'true');
        container.style.opacity = "1";
        button.classList.replace('btn-outline-secondary', 'btn-outline-success');
        statusText.innerText = 'Activo';
    }
}

function syncCheckpoints () {
    // Recolectar checkpoints iniciales
    const initialDataRaw = document.getElementById('checkpoints-initial-data').value;
    const initialCheckpoints = JSON.parse(initialDataRaw || "[]");

    // Obtener datos pendientes de localStorage
    const pendingDataRaw = localStorage.getItem('pending_checkpoints');
    const hasLocalStorage = pendingDataRaw != null;

    // 2. Obtener datos actuales (Priorizamos la memoria RAM global 'checkpoints' si existe, o el LocalStorage)
    let currentCheckpoints = [];
    if (typeof checkpoints !== 'undefined' && checkpoints.length > 0) {
        currentCheckpoints = checkpoints;
    } else if (hasLocalStorage){
        try{
            currentCheckpoints = JSON.parse(pendingDataRaw || "[]");
        } catch (e) {
            console.error("Error al parsear pending_checkpoints en sincronización:", e);
            currentCheckpoints = [];
        }
    }

    // Map para facilitar la busqueda por ID
    const finalList = new Map();
    /*
        Procesar los pendientes (nuevos y modificados)
        Los pendientes mandan spbre los modificados
    */
    currentCheckpoints.forEach(cp => {
        // Usa externalId si existe, si no, una llave temporal para el map
        const key = cp.externalId || `new_${Math.random()}`;

        const safeDescription = cp.description ? String(cp.description).trim() : "";

        finalList.set(key, {
            externalId: cp.externalId || null,
            name: cp.name.trim(),
            description: safeDescription,
            latitude: cp.latitude || 0.0,
            longitude: cp.longitude || 0.0,
            checkpointOrder: cp.checkpointOrder || 0.0,
            stayTime: cp.stayTime || 5,
            minutesToReach: cp.transitTime || 0,
            active: cp.active !== undefined ? cp.active : true,
            deleted: false
        })
    });
    /*
        Tratar eliminaciones
        Compara los checkpoints iniciales con los que estan en la lista final
        si un Id inicial no esta en los pendientes, significa que fue eliminado.
    */
    const sendData = [];
    // Agregar lo que esta en finalList
    finalList.forEach(cp => sendData.push(cp));

    /*
        Tratar eliminaciones de forma segura:
        🟢 CORREGIDO: Solo procesamos bajas si el usuario efectivamente interactuó (RAM o LocalStorage activos)
        y usamos 'currentCheckpoints' en lugar de la variable fantasma.
    */
    if (hasLocalStorage || currentCheckpoints.length > 0) {
        initialCheckpoints.forEach(ini => {
            const existYet = currentCheckpoints.some(p => p.externalId === ini.externalId);
            if(!existYet){
                // Enviar una bandera de eliminación
                sendData.push({
                    ...ini, 
                    deleted: true
                });
            }
        });
    }

    return sendData;
};

/**
 * Navega a la vista de mapa para capturar checkpoints.
 * Usa el siteId seleccionado en el formulario.
 */
function openMapPicker() {
    const uuid = document.getElementById('patrolExternalId').value;
    const siteSelect = document.getElementById('siteId'); // Tu select de sitios
    const siteId = siteSelect ? siteSelect.value : null;

    if (!siteId || siteId === "") {
        // Usar una alerta o un toast personalizado si tienes uno
        alert("Por favor, selecciona un sitio para poder ubicar los puntos en el mapa.");
        return;
    }

    console.log(`[Navigation] Accediendo al mapa para el sitio ID: ${siteId}`);
    
    // Navegación SPA usando la ruta con PathVariable que definimos en el Controller
    navigateTo(`/private/patrols/edit-map-picker/${uuid}/${siteId}`);
}

function addCheckpoints(point) {
    const container = qs('#checkpointsList');
    if(!container) return;

    const latFixed = Number(point.latitude || 0).toFixed(6);
    const lngFixed = Number(point.longitude || 0).toFixed(6);

    const div = document.createElement('div');
    div.className = 'checkpoint-item map-point';

    div.innerHTML = `
        <div class="checkpoint-coords">
            <i class="fa-solid fa-location-dot"></i>
            <code>${latFixed}, ${lngFixed}</code>
            <input type="hidden" name="checkpointLat[]" value="${point.latitude}" />
            <input type="hidden" name="checkpointLng[]" value="${point.longitude}" />
        </div>

        <div class="field text">${point.name || '-'}</div>
        <div class="field number">${point.stayTime || 5}</div>
        <div class="field number">${point.transitTime || 3}</div>

    `;

    container.appendChild(div);
}

function initCheckpoints() {
    const container = document.getElementById('checkpointsList');
    if(!container) return

    // Intenta obtener checkpoints del mapa
    const mapData = localStorage.getItem('pending_checkpoints');

    if(mapData) {
        console.log('[Analista] Cargando checkpoints desde el Map...');
        const points = JSON.parse(mapData);
        points.forEach(p => addCheckpoints(p));
        //localStorage.removeItem('pending_checkpoints');
    } else {
        console.log('[Analista] Cargando checkpoints desde la BD...');
        const dbInput = document.getElementById('checkpoints-initial-data');
        if(dbInput && dbInput.value && dbInput.value.trim()!=""){
            try{
                const points = JSON.parse(dbInput.value);
                console.log(`[Analista] Encontrados ${points.length} puntos en BD`);
                points.forEach(p => addCheckpoints(p));
            } catch (e) {
                console.warn("La BD no tiene JSON válido o está vacía");
            }
        } else {
            console.log('[Analista] No hay puntos previos ni en Mapa ni en BD.');
        }
    }
}

function bindEvents() {
    const updateBtn = qs('#updatePatrolBtn');
    if (updateBtn) {
        updateBtn.addEventListener('click', handleUpdate);
    }
    qs('#addTimeScheduleBtn').addEventListener('click', onClickAddTimeSchedule);
    qs('#checkpointsList')?.addEventListener('click', onClickRemoveItem);
    qs('#patrolSchedulesList')?.addEventListener('click', onClickRemoveItem);
    qs('#toggleSchedule')?.addEventListener('click', toggleSchedule);
    qs('#toggleCheckpoint')?.addEventListener('click', toggleCheckpoint);
    qs('#addMapCheckpointBtn')?.addEventListener('click', openMapPicker);

    // ESCUCHADOR: Espera a que el navigation-handler confirme la carga
    document.addEventListener('route:loaded', () => {
            initCheckpoints(); // <--- Llamamos a la función separada
    }, { once: true });

}

(function init() {
    bindEvents();
})();