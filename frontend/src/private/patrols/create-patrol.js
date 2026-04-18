import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);
const qsa = (s) => document.querySelectorAll(s);

async function onClickCreate(e) {
    e.preventDefault();

    // Contenedor de alertas
    const alertSuccess = qs('.alert-success');
    const alertError = qs('.alert-error');

    //Generar payload dinámico con los datos del formulario
    const payload = {
    siteId : Number(qs('#siteId')?.value),
    patrolName : qs('#patrolName')?.value?.trim(),
    dayFrom : Number(qs('#dayFrom')?.value),
    dayTo : Number(qs('#dayTo')?.value),
    startTime : qs('#startTime')?.value,
    // Recolectar Array de Horarios
    scheduleTimes : Array.from(qsa('input[name="scheduleTime[]"]'))
                               .map(input => input.value)
                               .filter(val => val !== ""),
    // Recolectar Array de Puntos de Control
    checkpoints : Array.from(qsa('input[name="checkpointName[]"]'))
                             .map(input => input.value.trim())
                             .filter(val => val !== "")
    };

    // Validaciones
    if (!siteId || !patrolName || !dayFrom || !dayTo || !startTime) {
        displayAlert(alertError, 'Por favor, complete todos los campos obligatorios.');
        return;
    }

    try {
        const response = await fetchWithAuth('/api/patrols/create', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify(payload)
        });

        // Verificar respuesta del servidor
        if (!response.ok) {
            const errorData = await response.json(); // Intentar capturar un mensaje detallado
            displayAlert(alertError,
                    `Error: ${errorData.message || 'Ocurrió un problema al enviar el formulario.'}`);
            return;
        }

        displayAlert(alertSuccess, '¡Formulario enviado correctamente!'); // Mostrar alerta de éxito

        setTimeout(() => {
            navigateTo('/private/patrols/table-view'); // recarga el listado
        }, 3000);

    } catch (error) {
        // Manejo de errores de red u otros errores inesperados
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.');
    }

}


async function onClickCancel(e) {
    e.preventDefault();

    const alertCancel = qs('.alert-cancel');

    displayAlert(alertCancel, 'La creación de la ronda de supervisión ha sido cancelada.');

    setTimeout(() => {
        navigateTo('/private/patrols/table-view'); // recarga el listado
    }, 3000);
}


function displayAlert(alertElement, message, timeout = 5000) {
    const alertContainer = qs('.patrol-alert-container'); // Selecciona el contenedor de alertas

    // Asegurar que el contenedor es visible
    alertContainer.style.display = 'block';

    // Actualizar el mensaje dentro de la alerta
    const alertMessage = alertElement.querySelector('.alert-message'); // Solo el texto dinámico
    alertMessage.textContent = message; // Establece el texto dinámico

    // Mostrar la alerta específica dinámica
    alertElement.classList.add('alert-show');

    // Ocultar automáticamente después del timeout
    setTimeout(() => {
        alertElement.classList.remove('alert-show'); // Remueve la clase de mostrar
        alertContainer.style.display = 'none'; // Esconde todo el contenedor
    }, timeout);
}


async function onClickAddTimeSchedule(e) {
    e.preventDefault();
    const container = qs('#patrolSchedulesList');
    const div = document.createElement('div');
    div.className = 'schedule';
    div.innerHTML = `
        <input type="time" name="scheduleTime[]" class="schedule-input" required />
        <button type="button" class="btn-mini btn-remove">Eliminar</button>
    `;
    container.appendChild(div);
}


// --- Lógica de Puntos de Control ---
function onClickAddCheckpoint(e) {
    e.preventDefault();
    const container = qs('#checkpointsList');
    if(!container) return; // Validación de seguridad

    const div = document.createElement('div');
    div.className = 'checkpoint-item';
    div.innerHTML = `
        <input type="text" name="checkpointName[]"
                placeholder="Nombre del punto (ej: Bodega A)" required />
        <button type="button" 
                class="btn-mini btn-remove">Eliminar</button>
    `;
    container.appendChild(div);
}


// Manejador genérico para eliminar elementos
function onClickRemoveItem(e) {
    // Verificamos si el clic fue en un botón de eliminar
    const isRemoveButton = e.target.classList.contains('btn-remove') || 
                           e.target.classList.contains('btn-remove-schedule');

    if (isRemoveButton) {
        // Usamos .closest() para encontrar el contenedor del ítem 
        // y asegurarnos de borrar el div correcto (checkpoint-item o schedule)
        const itemToRemove = e.target.closest('.checkpoint-item, .schedule');
        if (itemToRemove) {
            itemToRemove.remove();
        }
    }
}



function bindEvents() {
    // acciones principales
    qs('#createPatrolBtn')?.addEventListener('click', onClickCreate);
    qs('#cancelCreatePatrolBtn')?.addEventListener('click', onClickCancel);
    // Delegación de eventos para añadir puntos de control y horarios
    qs('#addTimeScheduleBtn')?.addEventListener('click', onClickAddTimeSchedule);
    qs('#addCheckpointBtn')?.addEventListener('click', onClickAddCheckpoint);
    /* Delegación de eventos para eliminar puntos de control y horarios
        se asigna al contenedor que ya existe en el HTML*/
    qs('#checkpointsList')?.addEventListener('click', onClickRemoveItem);
    qs('#patrolSchedulesList')?.addEventListener('click', onClickRemoveItem);
}

(function init() {
    bindEvents();
})();