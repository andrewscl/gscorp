import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { onClickAddTimeSchedule,
        onClickRemoveItem} from './create-patrol.js';
import { displayAlert } from '../../shared/display-alert.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js';

let patrolEditPathLine = null;
let checkpointsToDraw = [];

const qs  = (s) => document.querySelector(s);
const qsa = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

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

    let checkpointsToDraw = [];

    // Verificación A: ¿El picker dinámico ya dejó datos modificados en memoria?
    if (window.currentCheckpoints && window.currentCheckpoints.length > 0) {
        console.log("🔄 [Edit] Cargando puntos modificados en caliente desde el picker:", window.currentCheckpoints.length);
        checkpointsToDraw = window.currentCheckpoints;
    } 
    // Verificación B: Carga inicial. Buscamos el input del DOM que inyecta Thymeleaf
    else {
        const dataInput = document.getElementById('checkpoints-initial-data') || document.getElementById('checkpoints-data');
        if (dataInput && dataInput.value) {
            try {
                console.log("📁 [Edit] Cargando puntos iniciales desde el Backend.");
                checkpointsToDraw = JSON.parse(dataInput.value);
            } catch (e) {
                console.error("Error al parsear el JSON del input oculto:", e);
            }
        }
    }

    // Si ambos lados están vacíos, no hay nada que dibujar
    if (!checkpointsToDraw || checkpointsToDraw.length === 0) {
        console.warn("⚠️ No se encontraron puntos en el DOM ni en memoria.");
        return;
    }

    try {
        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");

        // Crear el objeto bounds
        const bounds = new google.maps.LatLngBounds();

        for (const cp of checkpointsToDraw) {
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
    const pathCoordinates = checkpointsToDraw.map(p => ({
                                lat: p.latitude, lng: p.longitude }));

    if (patrolEditPathLine) {
        patrolEditPathLine.setPath(pathCoordinates);
    } else {
        patrolEditPathLine = new google.maps.Polyline({
            path: pathCoordinates,
            geodesic: true,
            strokeColor: "#FF0000",
            strokeOpacity: 1.0,
            strokeWeight: 3,
            map: window.mapInstance
        });
    }
}


async function handleUpdate(e) {
    e.preventDefault();

    // Recolectar schedules con su estado activo/inactivo
        const schedules = Array.from(qsa('.schedule')).map(container => {
        const timeInput = container.querySelector('input[name="scheduleTime[]"]');
        const statusSpan = container.querySelector('.status-text');

        if(timeInput) {
            return {
                startTime: timeInput.value,
                // LÓGICA DE PROTECCIÓN:
                // Si 'statusSpan' existe (registro viejo), leemos su texto.
                // Si 'statusSpan' es null (registro nuevo), asignamos true automáticamente.
                active: statusSpan ? (statusSpan.innerText.trim() === "Activo") : true
            };
        }
        return null;

        }).filter(container => container !== null && container.startTime !== "");

    // Ejecutar sincronización y guardar el resultado
    const consolidatedCheckpoints = syncCheckpoints();
    console.log("Checkpoints a enviar:", JSON.stringify(consolidatedCheckpoints, null, 2));

    const externalId = qs('#patrolExternalId')?.value; // ID de la ronda a actualizar
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
        checkpoints: consolidatedCheckpoints
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
            displayAlert(alertSuccess, "Ronda actualizada correctamente.", 2000);
            setTimeout(() => {
                navigateTo('/private/patrols/table-view'); // Redirigir a la tabla
            }, 1500);
            localStorage.removeItem('pending_checkpoints');
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

function syncCheckpoints () {
    // Recolectar checkpoints iniciales
    const initialDataRaw = document.getElementById('checkpoints-initial-data').value;
    const initialCheckpoints = JSON.parse(initialDataRaw || "[]");

    // Obtener datos pendientes de localStorage
    const pendingDataRaw = localStorage.getItem('pending_checkpoints');
    const hasLocalStorage = pendingDataRaw != null;

    // 2. Obtener datos actuales (Priorizamos la memoria RAM global 'checkpoints' si existe, o el LocalStorage)
    let currentCheckpoints = [];
    if (typeof checkpoints !== 'undefined' && checkpoints.length > 0) {
        currentCheckpoints = checkpoints;
    } else if (hasLocalStorage){
        try{
            currentCheckpoints = JSON.parse(pendingDataRaw || "[]");
        } catch (e) {
            console.error("Error al parsear pending_checkpoints en sincronización:", e);
            currentCheckpoints = [];
        }
    }

    // Map para facilitar la busqueda por ID
    const finalList = new Map();
    /*
        Procesar los pendientes (nuevos y modificados)
        Los pendientes mandan spbre los modificados
    */
    currentCheckpoints.forEach(cp => {
        // Usa externalId si existe, si no, una llave temporal para el map
        const key = cp.externalId || `new_${Math.random()}`;

        const safeDescription = cp.description ? String(cp.description).trim() : "";

        finalList.set(key, {
            externalId: cp.externalId || null,
            name: cp.name.trim(),
            description: safeDescription,
            latitude: cp.latitude || 0.0,
            longitude: cp.longitude || 0.0,
            checkpointOrder: cp.checkpointOrder || 0.0,
            stayTime: cp.stayTime || 5,
            minutesToReach: cp.transitTime || 0,
            active: cp.active !== undefined ? cp.active : true,
            deleted: false
        })
    });
    /*
        Tratar eliminaciones
        Compara los checkpoints iniciales con los que estan en la lista final
        si un Id inicial no esta en los pendientes, significa que fue eliminado.
    */
    const sendData = [];
    // Agregar lo que esta en finalList
    finalList.forEach(cp => sendData.push(cp));

    /*
        Tratar eliminaciones de forma segura:
        🟢 CORREGIDO: Solo procesamos bajas si el usuario efectivamente interactuó (RAM o LocalStorage activos)
        y usamos 'currentCheckpoints' en lugar de la variable fantasma.
    */
    if (hasLocalStorage || currentCheckpoints.length > 0) {
        initialCheckpoints.forEach(ini => {
            const existYet = currentCheckpoints.some(p => p.externalId === ini.externalId);
            if(!existYet){
                // Enviar una bandera de eliminación
                sendData.push({
                    ...ini, 
                    deleted: true
                });
            }
        });
    }

    return sendData;
};

/**
 * Navega a la vista de mapa para capturar checkpoints.
 * Usa el siteId seleccionado en el formulario.
 */
function openMapPicker() {
    const uuid = document.getElementById('patrolExternalId').value;
    const siteSelect = document.getElementById('siteId'); // Tu select de sitios
    const siteId = siteSelect ? siteSelect.value : null;

    if (!siteId || siteId === "") {
        // Usar una alerta o un toast personalizado si tienes uno
        alert("Por favor, selecciona un sitio para poder ubicar los puntos en el mapa.");
        return;
    }

    console.log(`[Navigation] Accediendo al mapa para el sitio ID: ${siteId}`);
    
    // Navegación SPA usando la ruta con PathVariable que definimos en el Controller
    navigateTo(`/private/patrols/edit-map-picker/${uuid}/${siteId}`);
}

function addCheckpoints(point) {
    const container = qs('#checkpointsList');
    if(!container) return;

    const latFixed = Number(point.latitude || 0).toFixed(6);
    const lngFixed = Number(point.longitude || 0).toFixed(6);

    const div = document.createElement('div');
    div.className = 'checkpoint-item map-point';

    div.innerHTML = `
        <div class="checkpoint-coords">
            <input type="hidden" name="checkpointLat[]" value="${point.latitude}" />
            <input type="hidden" name="checkpointLng[]" value="${point.longitude}" />
        </div>

        <div class="field text">${point.name || '-'}</div>
        <div class="field number">${point.stayTime || 5}</div>
        <div class="field number">${point.transitTime || 3}</div>

    `;

    container.appendChild(div);
}

function initCheckpoints() {
    const container = document.getElementById('checkpointsList');
    if(!container) return

    // Intenta obtener checkpoints del mapa
    const mapData = localStorage.getItem('pending_checkpoints');

    if(mapData) {
        console.log('[Analista] Cargando checkpoints desde el Map...');
        const points = JSON.parse(mapData);
        points.forEach(p => addCheckpoints(p));
        //localStorage.removeItem('pending_checkpoints');
    } else {
        console.log('[Analista] Cargando checkpoints desde la BD...');
        const dbInput = document.getElementById('checkpoints-initial-data');
        if(dbInput && dbInput.value && dbInput.value.trim()!=""){
            try{
                const points = JSON.parse(dbInput.value);
                console.log(`[Analista] Encontrados ${points.length} puntos en BD`);
                points.forEach(p => addCheckpoints(p));
            } catch (e) {
                console.warn("La BD no tiene JSON válido o está vacía");
            }
        } else {
            console.log('[Analista] No hay puntos previos ni en Mapa ni en BD.');
        }
    }
}

function onClickCancel() {
    displayAlert(alertSuccess, "La edición de ronda ha sido concelada.", 2000);
    setTimeout(() => navigateTo('/private/patrols/table-view', true), 1500);
}

function bindEvents() {
    const updateBtn = qs('#updatePatrolBtn');
    if (updateBtn) {
        updateBtn.addEventListener('click', handleUpdate);
    }

    const cancelBtn = qs('#cancelEditPatrolBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', onClickCancel);
    }

    qs('#addTimeScheduleBtn').addEventListener('click', onClickAddTimeSchedule);
    qs('#checkpointsList')?.addEventListener('click', onClickRemoveItem);
    qs('#patrolSchedulesList')?.addEventListener('click', onClickRemoveItem);
    qs('#toggleCheckpoint')?.addEventListener('click', toggleCheckpoint);
    qs('#addMapCheckpointBtn')?.addEventListener('click', openMapPicker);

    // ESCUCHADOR: Espera a que el navigation-handler confirme la carga
    document.addEventListener('route:loaded', () => {
            initCheckpoints(); // <--- Llamamos a la función separada
    }, { once: true });

}

(async function init() {
    const apiKey = googleMapsConfig.apiKey;
    bindEvents();
    await startViewMap();
})();