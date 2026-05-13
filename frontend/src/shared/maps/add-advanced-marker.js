/**
 * Crea un marcador avanzado con tooltip personalizado.
 * @param {google.maps.Map} mapInst - Instancia del mapa donde se añadirá.
 * @param {string} name - Nombre a mostrar en el tooltip.
 * @param {number} lat - Latitud.
 * @param {number} lng - Longitud.
 * @returns {google.maps.marker.AdvancedMarkerElement}
 */
export const addAdvancedMarker = async (mapInst, name, lat, lng) => {
    const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");

    // 1. Tooltip (Contenido dinámico)
    const markerContent = document.createElement('div');
    markerContent.className = 'custom-map-tooltip'; // Usa una clase CSS para limpiar el JS
    markerContent.textContent = name || 'Sin Nombre';
    Object.assign(markerContent.style, {
        backgroundColor: '#fff', border: '1px solid grey', padding: '4px 8px',
        borderRadius: '8px', boxShadow: '0 2px 6px rgba(0,0,0,0.3)',
        position: 'absolute', top: '-40px', left: '50%',
        transform: 'translateX(-50%)', whiteSpace: 'nowrap', display: 'none'
    });

    // 2. Estilo del Pin
    const pin = new PinElement({
        scale: 0.8,
        glyphColor: '#3176e3',
        background: '#359dd1',
        borderColor: '#1d4d9b',
    });

    // 3. Contenedor Maestro
    const container = document.createElement('div');
    container.style.position = 'relative';
    container.appendChild(pin.element);
    container.appendChild(markerContent);

    // 4. Crear Marcador
    const marker = new AdvancedMarkerElement({
        map: mapInst,
        position: { lat: parseFloat(lat), lng: parseFloat(lng) },
        content: container,
    });

    // 5. Eventos
    container.addEventListener('mouseenter', () => markerContent.style.display = 'block');
    container.addEventListener('mouseleave', () => markerContent.style.display = 'none');

    return marker;
};