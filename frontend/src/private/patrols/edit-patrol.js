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
    
    const externalId = qs('#patrolExternalId')?.value; // ID de la ronda a actualizar
    // Contenedor de alertas
    const alertSuccess = qs('.alert-success');
    const alertError = qs('.alert-error');
    
    // 1. Recolección de datos (Payload)
    const payload = {
        siteId: parseInt(qs('#siteId').value),
        name: qs('#patrolName').value.trim(),
        description: qs('#patrolDescription').value.trim(),
        dayFrom: parseInt(qs('#dayFrom').value),
        dayTo: parseInt(qs('#dayTo').value),
        // Recolectar listas
        scheduleTimes: Array.from(qsa('input[name="scheduleTime[]"]'))
                            .map(input => input.value)
                            .filter(val => val !== ""),
        checkpoints: Array.from(qsa('input[name="checkpointName[]"]'))
                          .map(input => input.value.trim())
                          .filter(val => val !== "")
    };

    // 2. Validación básica
    if (!payload.name || payload.checkpoints.length === 0) {
        displayAlert(alertError,
                        "El nombre y al menos un punto de control son obligatorios.");
        return;
    }

    // 3. Envío al Servidor
    try {
        const response = await fetchWithAuth(`/api/patrols/${externalId}`, {
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