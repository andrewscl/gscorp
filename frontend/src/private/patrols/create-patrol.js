import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

async function onClickCreate(e) {
    e.preventDefault();

    // Contenedor de alertas
    const alertSuccess = qs('.alert-success');
    const alertError = qs('.alert-error');

    // Valores del formulario
    const siteId = Number(qs('#siteId')?.value);
    const patrolName = qs('#patrolName')?.value?.trim();
    const dayFrom = Number(qs('#dayFrom')?.value);
    const dayTo = Number(qs('#dayTo')?.value);
    const startTime = qs('#startTime')?.value;

    // Validaciones
    if (!siteId || !patrolName || !dayFrom || !dayTo || !startTime) {
        displayAlert(alertError, 'Por favor, complete todos los campos obligatorios.');
        return;
    }

    try {
        const response = await fetchWithAuth('/api/patrols/create', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({
                siteId,
                patrolName,
                dayFrom,
                dayTo,
                startTime
            })
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
            navigateTo('/private/sites/table-view'); // recarga el listado
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
        navigateTo('/private/sites/table-view'); // recarga el listado
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


function bindEvents() {
    qs('#createPatrolBtn')?.addEventListener('click', onClickCreate);
    qs('#cancelCreatePatrolBtn')?.addEventListener('click', onClickCancel);
}

(function init() {
    bindEvents();
})();