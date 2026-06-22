import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js';

let checkpoints = [] //Lista de objetos {lat, lng, order}
let checkpointMarkers = [] //referencia a los marcadores
let patrolPathLine = null;
let currentInfoWindow = null;

const qs  = (s) => document.querySelector(s);

const backToPatrolList = () => {
    setTimeout(() => navigateTo('/private/patrols/table-view', true), 1000);
}

const startViewMap = async () => {
  const apiKey = googleMapsConfig.apiKey;
  const id = qs('#siteId').value;

  try {
    console.log('Loading Google Maps API...');
    await loadGoogleMapsAPI(apiKey);

    const map = await initMap('map', {
      mapTypeId: 'hybrid',
      zoom: 10,
    });

    window.mapInstance = map;
    mapInstance = map;

    // 🟢 TRUCO DE UX: Si el usuario pincha el mapa vacío, se cierra el InfoWindow abierto
    map.addListener('click', () => {
        if (currentInfoWindow) currentInfoWindow.close();
    });

    const response = await fetchWithAuth(`/api/sites/${id}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    const siteData = await response.json();

    console.log('Site data:', siteData);
    const initialMarker = await addAdvancedMarker(map, siteData.name, siteData.lat, siteData.lon);

    const bounds = new google.maps.LatLngBounds();
    bounds.extend({ lat: parseFloat(siteData.lat), lng: parseFloat(siteData.lon) });
    map.fitBounds(bounds);
    map.setZoom(15);

    //Obtener y cargar checkpoints existentes
    await loadExistingCheckpoints();

    return { map, siteData, initialMarker };

  } catch (error) {
    console.error('[site-map.js] Error al cargar la API de Google Maps:'
                                                                    , error);
  }
}

const loadExistingCheckpoints = async () => {
    // 1. Buscamos el input oculto
    const dataInput = document.getElementById('checkpoints-data');
    if (!dataInput || !dataInput.value) {
        console.warn("No se encontraron puntos pre-cargados en el DOM.");
        return;
    }

    try {
        // 2. Parseamos el JSON
        const preloaded = JSON.parse(dataInput.value);
        if (preloaded.length === 0) return;

        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");

        // 🟢 Inicializar el InfoWindow global si no existe
        if (!currentInfoWindow) {
            currentInfoWindow = new google.maps.InfoWindow();
        }

        // Crear el objeto bounds
        const bounds = new google.maps.LatLngBounds();

        for (const cp of preloaded) {
            const position = {
                lat: parseFloat(cp.latitude),
                lng: parseFloat(cp.longitude) 
            };

            // Extender los limites para incluir la posicion
            bounds.extend(position);

            // Dibujar marcador (tu lógica existente)
            const pin = new PinElement({
                glyph: cp.checkpointOrder.toString(),
                background: "#FBBC04",
                borderColor: "#137333",
                glyphColor: "white",
            });

            const marker = new AdvancedMarkerElement({
                map: window.mapInstance,
                position: position,
                content: pin.element,
                title: cp.name,
                gmpDraggable: false,
            });

            // 🟢 ESCUCHADOR DE CLIC PARA CADA MARCADOR AMARILLO
            marker.addListener('click', () => {
                currentInfoWindow.close(); // Cerrar el anterior

                // Contenido HTML elegante en modo Solo Lectura
                const infoContent = `
                    <div style="font-family: 'Segoe UI', Roboto, sans-serif; padding: 6px; min-width: 170px; color: #1e293b;">
                        <div style="border-bottom: 2px solid #3b82f6; padding-bottom: 4px; margin-bottom: 6px;">
                            <strong style="font-size: 13px; display: block; color: #1e3a8a;">${cp.name}</strong>
                        </div>
                        <div style="font-size: 11px; line-height: 1.5;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                                <span style="color: #64748b;">Descripción:</span>
                                <span style="font-weight: 600; color: #0f172a;">${cp.description}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                                <span style="color: #64748b;">Orden:</span>
                                <span style="font-weight: 600; color: #0f172a;">N° ${cp.checkpointOrder}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                                <span style="color: #64748b;">Estadía:</span>
                                <span style="font-weight: 600; color: #0f172a;">${cp.stayTime} min</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: #64748b;">Tránsito:</span>
                                <span style="font-weight: 600; color: #0f172a;">${cp.minutesToReach || 0} min</span>
                            </div>
                        </div>
                    </div>
                `;

                currentInfoWindow.setContent(infoContent);
                currentInfoWindow.open({
                    map: window.mapInstance,
                    anchor: marker
                });
            });

            // Sincronizar arrays globales del JS
            checkpoints.push({
                externalId: cp.externalId,
                latitude: position.lat,
                longitude: position.lng,
                checkpointOrder: cp.checkpointOrder,
                name: cp.name,
                stayTime: cp.stayTime,
                transitTime: cp.minutesToReach
            });
            checkpointMarkers.push(marker);
        }

        // 3. Actualizar visuales
        updatePathLine();

        // Centrar mapa en el primer punto si existe
        window.mapInstance.fitBounds(bounds);

    } catch (e) {
        console.error("Error al parsear los puntos ocultos:", e);
    }
};

function updatePathLine() {
    const pathCoordinates = checkpoints.map(p => ({
                                lat: p.latitude, lng: p.longitude }));

    if (patrolPathLine) {
        patrolPathLine.setPath(pathCoordinates);
    } else {
        patrolPathLine = new google.maps.Polyline({
            path: pathCoordinates,
            geodesic: true,
            strokeColor: "#FF0000",
            strokeOpacity: 1.0,
            strokeWeight: 3,
            map: window.mapInstance
        });
    }
}

function bindViewPatrol() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', backToPatrolList);
    }
}


(async function init() {
    console.log('[view-patrol init] IIFE iniciado');
    const apiKey = googleMapsConfig.apiKey;

    bindViewPatrol();

    await startViewMap();
    console.log('View patrol page initialized.');



})();