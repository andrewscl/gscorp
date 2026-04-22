import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

let checkpoints = [] //Lista de objetos {lat, lng, order}
let checkpointMarkers = [] //referencia a los marcadores
let patrolPathLine = null;
let currentInfoWindow = null;

const qs  = (s) => document.querySelector(s);

// Función para cargar el script de Google Maps - Moderno y modular
const loadGoogleMapsAPI = (() => {
  let isScriptLoaded = false; // Bandera para evitar duplicados
  let scriptPromise = null; // Promesa para gestionar la carga del script

  return (apiKey) => {
    if (isScriptLoaded) {
      return scriptPromise; // Si el script ya está cargado, devuelve la promesa
    }

    // Crear la promesa de carga del script
    scriptPromise = new Promise((resolve, reject) => {
      // Crear el script dinámicamente
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&v=weekly`;
      script.async = true;
      script.defer = true;

      // Resolver cuando el script se cargue correctamente
      script.onload = () => {
        console.log('[Google Maps API] Script cargado correctamente.');
        isScriptLoaded = true;
        resolve();
      };

      // Rechazar si ocurre algún error al cargar el script
      script.onerror = (error) => {
        console.error('[Google Maps API] Error al cargar el script:', error);
        reject(new Error('Error al cargar Google Maps API'));
      };

      // Adjuntar el script al <head>
      document.head.appendChild(script);
    });

    return scriptPromise; // Devolver la promesa para su uso
  };
})();

// Función para inicializar el mapa
const initMap = async () => {
  const mapContainer = document.getElementById('patrols-map-picker'); // Div contenedor del mapa
  if (!mapContainer) {
    console.warn('[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.');
    return;
  }

  console.log('siteId en initMap: ' + window.targetSiteId);

  try {
    // Importar las bibliotecas necesarias usando el enfoque moderno de Google
    const { Map } = await google.maps.importLibrary("maps");

    // Crear e inicializar el mapa
    const map = new Map(mapContainer, {
      center: { lat: -33.4489, lng: -70.6693 },
      zoom: 8, // Nivel de zoom inicial
      mapId: googleMapsConfig.mapId, // Personaliza con Map ID de Google Cloud
      disableDefaultUI: true, // Desactiva los controles predeterminados
      mapTypeId: 'hybrid',
    });
    console.log('[initMap] Mapa inicializado.');

    window.mapInstance = map; // Guardar la instancia del mapa globalmente

    // Obtener y añadir los sitios al mapa
    await fetchTargetSite();

    map.addListener("click", (event) => {
      addCheckpoint(event.latLng);
    });

  } catch (error) {
    console.error('[initMap] Error al inicializar el mapa:', error);
  }
};

// Obtiene la información del sitio desde el servidor usando fetchWithAuth
async function fetchTargetSite() {
  const input = document.getElementById('target-site-id');
  const siteId = input ? input.value : null;

  if (!siteId) {
        console.error("No se pudo obtener el ID del sitio del input oculto");
        return;
  }

  try {
    const res = await fetchWithAuth('/api/sites/projections-by-user', {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!res.ok) {
      console.log('Error al cargar sitios. Intente nuevamente.');
      return;
    }

    const siteData = await res.json();

    // Valida que es un arreglo y que los elementos tienen las propiedades necesarias
    if (!Array.isArray(siteData) || !siteData.every(site => site.id && site.lat && site.lon)) {
    console.log('Datos de sitios inválidos.');
    return;
    }

    console.log("Buscando siteId:", siteId);
    console.log("IDs disponibles en siteData:", siteData.map(s => s.id));

    const site = siteData.find(s => Number(s.id) === Number(siteId));

    console.log('site: ' + site);

    addSiteToMapAndSelect(site);

  } catch (error) {
    console.error('Fallo al obtener sitios:', error);
    console.log('Error al cargar sitios. Intente nuevamente.');
  }
}

// Función para agregar sitios al mapa como marcadores
async function addSiteToMapAndSelect(site) {

    const { AdvancedMarkerElement, PinElement } = 
                    await google.maps.importLibrary("marker");

        // crear una instancia para LatLngBounds
    const bounds = new google.maps.LatLngBounds();

    // Crear contenido personalizado para el tooltip
    const markerContent = document.createElement('div');
    markerContent.style.backgroundColor = '#fff';
    markerContent.style.border = '1px solid grey';
    markerContent.style.padding = '4px 8px';
    markerContent.style.borderRadius = '8px';
    markerContent.style.boxShadow = '0 2px 6px rgba(0,0,0,0.3)';
    markerContent.style.position = 'absolute';
    markerContent.style.top = '-40px'; // Encima del pin
    markerContent.style.left = '50%';
    markerContent.style.transform = 'translateX(-50%)';
    markerContent.style.whiteSpace = 'nowrap';
    markerContent.style.display = 'none'; // Ocultar inicialmente
    markerContent.textContent = `Sitio: ${site.name || 'Sin Nombre'}`; // Texto dinámico

    // Crear estilo del marcador (PinElement)
    const pin = new PinElement({
      scale: 0.8,
      glyphColor: '#3176e3',
      background: '#359dd1',
      borderColor: '#1d4d9b',
    });

    // Crear contenedor para el marcador y el contenido dinámico
    const markerContainer = document.createElement('div');
    markerContainer.style.position = 'relative';
    markerContainer.appendChild(pin.element); // Añadir el PinElement
    markerContainer.appendChild(markerContent); // Añadir el tooltip

    // Crear el marcador avanzado
    const advancedMarker = new AdvancedMarkerElement({
      map: window.mapInstance, // Asociar al mapa inicializado
      position: { lat: site.lat, lng: site.lon }, // Posición del sitio
      content: markerContainer, // Contenedor para el pin y el contenido
    });

    // Hover para mostrar el tooltip
    markerContainer.addEventListener('mouseenter', () => {
      markerContent.style.display = 'block'; // Mostrar el tooltip
    });

    markerContainer.addEventListener('mouseleave', () => {
      markerContent.style.display = 'none'; // Ocultar el tooltip
    });

    // Agregar las coordenadas del sitio a bounds
    bounds.extend(new google.maps.LatLng(site.lat, site.lon));

    console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${site.id}`);

    // Ajustar el mapa para mostrar el sitio
    window.mapInstance.fitBounds(bounds);

}

async function showInfoWindow(marker, index) {
    const { InfoWindow } = await google.maps.importLibrary("maps");

    // Cerrar el anterior si existe
    if (currentInfoWindow) currentInfoWindow.close();

    currentInfoWindow = new InfoWindow({
        content: `
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${index + 1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${index}" 
                           value="${point.name || ''}" 
                           oninput="updateCheckpointData(${index}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${point.stayTime || 5}" 
                               oninput="updateCheckpointData(${index}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${transitDisplay}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${point.transitTime || 3}" 
                               oninput="updateCheckpointData(${index}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${index})">
                    Eliminar Punto
                </button>
            </div>`
    });

    currentInfoWindow.open(window.mapInstance, marker);
}

async function addCheckpoint (latLng) {
    const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");

    const order = checkpoints.length + 1;

    // 1. Crear un estilo diferente para que no se confunda con el sitio base
    const pin = new PinElement({
        glyph: order.toString(), // Muestra el número de la ronda
        background: "#FBBC04",    // Color amarillo/naranja para checkpoints
        borderColor: "#137333",
        glyphColor: "white",
    });

    // 2. Crear el marcador en el mapa
    const marker = new AdvancedMarkerElement({
        map: window.mapInstance,
        position: latLng,
        content: pin.element,
        title: `Punto de control ${order}`,
    });

    // --- Listener  ---
    marker.addListener("click", () => {
        const index = checkpointMarkers.indexOf(marker);
        console.log("Marcador clickeado. Índice encontrado:", index);
        console.log("Total marcadores en array:", checkpointMarkers.length);
        
        if (index !== -1) {
            showInfoWindow(marker, index);
        } else {
            console.error("Error: El marcador clickeado no existe en checkpointMarkers.");
        }
    });

    // 3. Guardar en nuestros arrays
    checkpoints.push({
        lat: latLng.lat(),
        lng: latLng.lng(),
        order: order,
        name: `Punto ${order}`, // Nombre por defecto para que no nazca vacío
        stayTime: 5,            // 5 minutos de permanencia por defecto
        transitTime: order === 1 ? 0 : 3 // 0 si es el primero, 3 min si es traslado
    });

    checkpointMarkers.push(marker);

    updateCheckpointTable();
    updatePathLine();
    console.log("Checkpoints actuales:", checkpoints);
}

function updateCheckpointTable(){
    const tbody = document.getElementById('checkpoint-list-body');
    tbody.innerHTML = ''; // Limpiar

    checkpoints.forEach((point, index) => {
        // Lógica para el tiempo de tránsito (el primer punto no tiene tránsito previo)
        const transitText = index === 0 ? 
            '<span class="text-muted">---</span>' : 
            `${point.transitTime || 0} min`;

        const row = `
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${index + 1}</span>
                </td>
                <td>
                    <div class="fw-bold text-truncate" style="max-width: 150px;" title="${point.name || 'Sin nombre'}">
                        ${point.name || '<i class="text-muted">Punto sin nombre</i>'}
                    </div>
                </td>
                <td class="small text-muted">
                    ${point.lat.toFixed(5)}, ${point.lng.toFixed(5)}
                </td>
                <td class="text-center">
                    ${point.stayTime || 0} min
                </td>
                <td class="text-center">
                    ${transitText}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${index})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;
        tbody.insertAdjacentHTML('beforeend', row);
    });

}

function setupButtons() {
    document.getElementById('btn-clear-path')?.addEventListener('click', () => {
        if(confirm("¿Borrar todos los puntos?")) clearAllCheckpoints();
    });

    qs('#btn-confirm-map')
          .addEventListener('click', handleConfirmAndExit);
}

window.removeCheckpoint = function(index) {
    // 1. Validación de seguridad y quitar del mapa físico
    if (checkpointMarkers[index]) {
        checkpointMarkers[index].setMap(null);
    }
    
    // 2. Eliminar de los arrays de estado
    checkpoints.splice(index, 1);
    checkpointMarkers.splice(index, 1);
    
    // 3. Re-indexar el orden lógico (para que la data sea consistente)
    checkpoints.forEach((p, i) => p.order = i + 1);
    
    // 4. EL PASO CLAVE: Redibujar visuales
    // Opción A: redrawMap() -> Si borras y creas todo de nuevo
    // Opción B: updateMarkersAndPath() -> Si solo actualizas lo que quedó
    redrawMarkers();  // Actualiza los números (1, 2, 3...) en los pines amarillos
    updatePathLine(); // Recalcula la línea roja para que no queden huecos
    updateCheckpointTable(); // Refresca la tabla inferior
    
    // Cerramos cualquier InfoWindow abierto para que no quede flotando
    if (currentInfoWindow) currentInfoWindow.close();
};

function redrawMarkers() {
    // Recorremos los marcadores que quedaron para actualizar su número visual
    checkpointMarkers.forEach((marker, i) => {
        const order = i + 1;
        
        // Creamos el nuevo pin con el número actualizado
        const pin = new google.maps.marker.PinElement({
            glyphText: order.toString(),
            background: "#FBBC04"
        });
        
        // Actualizamos el contenido del marcador sin borrarlo del mapa
        marker.content = pin.element;
        marker.title = `Punto ${order}`;
        
        // MUY IMPORTANTE: Actualizamos el evento click del marcador 
        // para que apunte al nuevo índice, de lo contrario borraría el punto equivocado
        google.maps.event.clearInstanceListeners(marker);
        marker.addListener("click", () => {
            showInfoWindow(marker, i); // Función auxiliar para mostrar el botón eliminar
        });
    });
}

function updatePathLine() {
    const pathCoordinates = checkpoints.map(p => ({ lat: p.lat, lng: p.lng }));

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

function clearAllCheckpoints() {
    // 1. Quitar todos los marcadores del mapa
    checkpointMarkers.forEach(m => m.setMap(null));
    
    // 2. Resetear arrays
    checkpointMarkers = [];
    checkpoints = [];
    
    // 3. Limpiar línea
    if (patrolPathLine) {
        patrolPathLine.setPath([]);
    }
    
    // 4. Actualizar tabla
    updateCheckpointTable();
    
    if (currentInfoWindow) currentInfoWindow.close();
    console.log("Ruta reseteada correctamente.");
}

async function handleConfirmAndExit() {
if (checkpoints.length === 0) {
        alert("Define al menos un punto en la ruta antes de confirmar.");
        return;
    }

    // 1. Persistimos los datos para la siguiente "pantalla"
    localStorage.setItem('pending_checkpoints', JSON.stringify(checkpoints));

    // 2. Obtenemos el ID para la ruta
    const patrolExternalId = document.getElementById('target-patrol-externalId').value;
    const path = `/private/patrols/edit/${patrolExternalId}`;

    // 3. Navegación controlada (maneja el token y el fragmento automáticamente)
    console.log(`[MapPicker] Finalizando edición. Navegando a ${path}`);
    await navigateTo(path);
}

/* --- init --- */
(async function init() {

  console.log('[init] IIFE iniciado');

  const apiKey = googleMapsConfig.apiKey;

  try {
    //Cargar y esperar Google Maps API
    await loadGoogleMapsAPI(apiKey);

    // Inicializar el mapa
    initMap();
    setupButtons();

  } catch (error) {
    console.error('[site-map.js] Error al cargar la API de Google Maps:', error);
  }

})();