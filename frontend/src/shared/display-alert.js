export function displayAlert(
                    alertElement,
                    message,
                    timeout = 5000) {
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