import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { onClickAddTimeSchedule,
        onClickAddCheckpoint,
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

        if(!timeInput) return null;

        return {
            startTime: timeInput.value,
            active: statusSpan.innerText === 'Activo' // Determinar estado por el texto
        };
    }).filter(sch => sch.startTime !== ""); // Filtrar horarios vacíos

    // Recolectar checkpoints como objetos
    const checkpoints = Array.from(qsa('.checkpoint-item')).map(container => {
        const inputName = container.querySelector('input[name="checkpointName[]"]');
        const statusSpan = container.querySelector('.status-text');

        // Validación de seguridad para evitar el error 'reading value'
        if (!inputName) return null;

        return {
            siteId: parseInt(qs('#siteId').value),
            name: inputName.value.trim(),
            latitude: 0.0,
            longitude: 0.0,
            minutesToReach: 0,
            // Si hay statusSpan usamos su texto, si no (caso de nuevos), es true
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


function bindEvents() {
    // Botón Guardar Cambios
    const updateBtn = qs('#updatePatrolBtn');
    if (updateBtn) {
        updateBtn.addEventListener('click', handleUpdate);
    }

    // Botones dinámicos (Añadir/Eliminar)
    qs('#addTimeScheduleBtn').addEventListener('click', onClickAddTimeSchedule);
    qs('#addCheckpointBtn').addEventListener('click', onClickAddCheckpoint);

    /* Delegación de eventos para eliminar puntos de control y horarios*/
    qs('#checkpointsList')?.addEventListener('click', onClickRemoveItem);
    qs('#patrolSchedulesList')?.addEventListener('click', onClickRemoveItem);

    /* Delegación de eventos para alternar estado de horarios */
    qs('#toggleSchedule')?.addEventListener('click', toggleSchedule);
    qs('#toggleCheckpoint')?.addEventListener('click', toggleCheckpoint);

    // Botón Cancelar con mensaje personalizado
    const cancelBtn = qs('#cancelEditPatrolBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => 
            onClickCancel(e, 'La edición de la ronda ha sido cancelada.')
        );
    }
}

(function init() {
    bindEvents();
})();