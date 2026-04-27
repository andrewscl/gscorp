import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { onClickAddTimeSchedule,
        onClickRemoveItem,
        onClickCancel,
        displayAlert
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

    // Recolectar checkpoints como objetos
    const checkpoints = Array.from(qsa('.checkpoint-item')).map((container, index) => {
        const inputName = container.querySelector('input[name="checkpointName[]"]');
        const inputLat = container.querySelector('input[name="checkpointLat[]"]');
        const inputLng = container.querySelector('input[name="checkpointLng[]"]');
        const inputStay = container.querySelector('input[name="checkpointStayTime[]"]');
        const inputTransit = container.querySelector('input[name="checkpointTransitTime[]"]');
        const statusSpan = container.querySelector('.status-text');

        if (!inputName) return null;

        return {
            externalId: container.querySelector('input[name="checkpointExternalId[]"]')?.value || null,
            name: inputName.value.trim(),
            latitude: inputLat ? parseFloat(inputLat.value) : 0.0,
            longitude: inputLng ? parseFloat(inputLng.value) : 0.0,
            checkpointOrder: index + 1,
            stayTime: inputStay ? parseInt(inputStay.value) : 5,
            minutesToReach: inputTransit ? parseInt(inputTransit.value) : 0, // TransitTime
            active: statusSpan ? (statusSpan.innerText.trim() === 'Activo') : true,
        };
    }).filter(cp => cp !== null && cp.name !== "");

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
        checkpoints: checkpoints
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

    const div = document.createElement('div');
    div.className = 'checkpoint-item map-point'; // Clase extra para saber que viene del mapa

    div.innerHTML = `
        <div class="checkpoint-info">
            <small>${point.lat.toFixed(5)}, ${point.lng.toFixed(5)}</small>
        </div>
        
        <input type="hidden" name="checkpointLat[]" value="${point.lat}" />
        <input type="hidden" name="checkpointLng[]" value="${point.lng}" />
        
        <div class="checkpoint-inputs" style="display: flex; gap: 5px; flex-grow: 1;">
            <input type="text" name="checkpointName[]" 
                   placeholder="Nombre" value="${point.name || ''}" required />
            
            <input type="number" name="checkpointStayTime[]" 
                   title="Permanencia (min)" value="${point.stayTime || 5}" 
                   style="width: 60px;" required />
            
            <input type="number" name="checkpointTransitTime[]" 
                   title="Tránsito (min)" value="${point.transitTime || 3}" 
                   style="width: 60px;" required />
        </div>

        <button type="button" class="btn-mini btn-remove">Eliminar</button>
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
        localStorage.removeItem('pending_checkpoints');
    } else {
        console.log('[Analista] Cargando checkpoints desde la BD...');
        const dbInput = document.getElementById('checkpoints-initial-data');
        if(dbInput && dbInput.value){
            const points = JSON.parse(dbInput.value);
            points.forEach(p => addCheckpoints(p));
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
    // Botón Cancelar con mensaje personalizado
    const cancelBtn = qs('#cancelEditPatrolBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => 
            onClickCancel(e, 'La edición de la ronda ha sido cancelada.')
        );
    }
    qs('#addMapCheckpointBtn')?.addEventListener('click', openMapPicker);

    // ESCUCHADOR: Espera a que el navigation-handler confirme la carga
    document.addEventListener('route:loaded', () => {
            initCheckpoints(); // <--- Llamamos a la función separada
    }, { once: true });

}

(function init() {
    bindEvents();
})();