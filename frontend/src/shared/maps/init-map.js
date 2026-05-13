export const initMap = async (containerId, options = {}) => {
    const mapContainer = document.getElementById(containerId);
    if (!mapContainer) {
        console.warn(`[initMap] Contenedor #${containerId} no encontrado.`);
        return null;
    }

    try {
        const { Map } = await google.maps.importLibrary("maps");
        
        // Configuraciones por defecto mezcladas con las recibidas
        const map = new Map(mapContainer, {
            center: options.center || { lat: -33.4489, lng: -70.6693 },
            zoom: options.zoom || 12,
            mapId: options.mapId || googleMapsConfig.mapId,
            disableDefaultUI: options.disableDefaultUI !== undefined ? 
                                            options.disableDefaultUI : true,
        });

        console.log(`[initMap] Mapa en #${containerId} inicializado.`);
        return map; // Devuelve la instancia para poder usarla fuera
    } catch (error) {
        console.error('[initMap] Error:', error);
        throw error;
    }
};



