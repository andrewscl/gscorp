export const enableMarkerDrag = (marker, callback) => {
    marker.gmpDraggable = true;

    marker.addListener('dragend', () => {
        const coords = marker.position;

        // Comprobamos: ¿Existe 'callback' Y es de tipo 'function'?
        if (callback && typeof callback === 'function') {
            callback(coords); // Solo aquí la ejecutamos
        } else {
            console.warn("No se proporcionó una función válida para el movimiento del marcador.");
        }
    });
};