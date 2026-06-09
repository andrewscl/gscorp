import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js';
import { displayAlert } from '../../shared/display-alert.js';

let googleMapInstance = null;
let userMarkerInstance = null;
let sitesList = [];
const MAX_DISTANCE_GEOFENCE = 35;

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');


(async function init() {
  bindEvents();
  await initComponent();
  console.log('View attendance page initialized.');
})();


export const startViewMap = async (nearestSite) => {
  const apiKey = googleMapsConfig.apiKey;

  try {
    if (!googleMapInstance) {
      console.log('Loading Google Maps API...');
      await loadGoogleMapsAPI(apiKey);

      googleMapInstance = await initMap('map', {
        mapTypeId: 'hybrid',
        zoom: 10,
        center: {lat: -33.4489, lng: -70.6693}
      });
    }

    if (!nearestSite) return { map: googleMapInstance};

    if (userMarkerInstance){
      userMarkerInstance.setMap(null);
    }

    userMarkerInstance = await addAdvancedMarker(googleMapInstance, nearestSite.name, nearestSite.lat, nearestSite.lon);

// ⚡ MEJORA CRÍTICA: En lugar de fitBounds, centramos instantáneamente al zoom deseado
    googleMapInstance.setCenter({ lat: parseFloat(nearestSite.lat), lng: parseFloat(nearestSite.lon) });
    googleMapInstance.setZoom(17); // Zoom óptimo y rápido para geocercas

    return { map: googleMapInstance, nearestSite, initialMarker: userMarkerInstance };

  } catch (error) {
    console.error('[site-map.js] Error al cargar la API de Google Maps:'
                                                                    , error);
  }
}


async function initComponent() {

  setButtonsState(null, null);
  displayAlert(alertInfo,
        'Conectando con el servicio de Georreferenciación...', 1500);

  try {
    await loadSites();

    await startViewMap(null); 

    await defineCurrentPosition();
  } catch (e) {
    console.error('[attendance] initComponent failed', e); 
  }
}

async function syncAttendanceButtons() {
  try {
    const res = await fetchWithAuth('/api/attendance/last-punch');
    if (!res) throw new Error ('No se pudo obtener el ultimo estado.');
    const lastPunch = await res.json();

    console.log('[DEBUG] lastPunch:', lastPunch);
    console.log('[DEBUG] lastPunch.action:', lastPunch.action);

    const action = lastPunch?.action ? String(lastPunch.action).toUpperCase() : 'OUT';
    console.log('[DEBUG] action:', action);

    if (action === 'IN') {
      // Está marcado IN → mostrar botón de SALIDA
      console.log('[DEBUG] Showing OUT button (user is IN)');
      setButtonsState(null, true);

    } else {
      // Está marcado OUT o sin registros → mostrar botón de ENTRADA
      console.log('[DEBUG] Showing IN button (user is OUT or no records)');
      setButtonsState(true, null);
    }
  } catch (e) {
    console.error('[Attendance] Error al consigurar los botones');
    displayAlert(alertError, 'Error al sincronizar el estado del usuario', 4000);
    setButtonsState(false, false);
  }
}

async function defineCurrentPosition() {
  if (!navigator.geolocation) {
    displayAlert(alertError, 'Geolocalización no soportada.', 3000);    
    return;
  }
  displayAlert(alertInfo, 'Detectando el sitio mas cercano...', 3000);

  try {
      const pos = await new Promise((resolve, reject) =>
        navigator.geolocation.getCurrentPosition(resolve, reject,
                                    { enableHighAccuracy: true, timeout: 15000 })
      );
      if (!sitesList.length) {
        displayAlert(alertError, 'No hay sitios configurados.', 3000);
        return;
      }
      const nearestSite = getNearestSite(pos.coords.latitude,
                                              pos.coords.longitude, sitesList);
      if (nearestSite && nearestSite.distance <= MAX_DISTANCE_GEOFENCE) {
        displayAlert(alertSuccess, `Estás en el sitio "${nearestSite.name}".
                                          Puedes marcar asistencia aquí.`, 3000);

      if(nearestSite){
        await startViewMap(nearestSite);
      }

      await syncAttendanceButtons();

      } else if (nearestSite) {
        displayAlert(alertInfo, `El sitio más cercano es "${nearestSite.name}" a
            ${nearestSite.distance.toFixed(1)} metros. Acércate para marcar.`, 3000);

        if(nearestSite){
        await startViewMap(nearestSite);
        }

        setTimeout(() => navigateTo('/private/employees/dashboard'), 1500);
      } else {
        displayAlert(alertError, 'No se encontró ningún sitio cercano.', 5000);
      }
  } catch (e) {
    displayAlert(alertError, 'No fue posible obtener la ubicación: '
                      + (e.message || 'Tiempo de espera agotado.'), 5000);
  }
}

async function loadSites() {
  try {
    const res = await fetchWithAuth('/api/sites/user-sites', { credentials: 'same-origin' });
    if (!res.ok) throw new Error(`Error cargando sitios: ${res.status}`);
    sitesList = await res.json();
  } catch (e) {
    console.error("No se pudo cargar la lista de sitios:", e);
    sitesList = [];
  }
}

async function punch(kind) {
    const action = (String(kind).toUpperCase() === 'OUT') ? 'OUT' : 'IN';
    const actionWidget = qs('#att-widget');
    const endpoint = actionWidget?.dataset.punchEndpoint || '/api/attendance/punch';
    try {
      setButtonsState(false, false);
      displayAlert(alertInfo, 'Obteniendo ubicación...', 3000);
      const pos = await new Promise((resolve, reject) =>
        navigator.geolocation.getCurrentPosition(resolve, reject,
                                    { enableHighAccuracy: true, timeout: 15000 })
      );
      const nearestSite = getNearestSite(pos.coords.latitude, pos.coords.longitude, sitesList);
      if (!nearestSite || nearestSite.distance > MAX_DISTANCE_GEOFENCE) {
        displayAlert(alertWarning,
                `No puede marcar asistencia: está a ${nearestSite ? nearestSite.distance.toFixed(1) :
                                                  'N/A'} metros del sitio más cercano (máx 35m).`, 3000);
        await syncAttendanceButtons();
        return;
      } else {
        displayAlert(alertInfo, `Marcando asistencia en el sitio: ${nearestSite.name}`, 3000);
      }
      const payload = {
        action, // "IN" | "OUT"
        lat: pos.coords.latitude,
        lon: pos.coords.longitude,
        accuracy: pos.coords.accuracy,
        siteId: nearestSite.id
      };
      displayAlert(alertInfo, 'Registrando asistencia...', 3000);
      // Timeout de red (20s)
      const ac = new AbortController();
      const t  = setTimeout(() => ac.abort('timeout'), 20000);
      const res = await fetchWithAuth(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload),
        signal: ac.signal,
        credentials: 'same-origin'
      }).finally(() => clearTimeout(t));

      let resData = {};
      try {
        resData = await res.json();
      } catch {
        try { resData = { textFallback: await res.text() }; } catch {}
      }

      if (!res.ok) {
        let reason = `Error ${res.status}`;
        if (resData?.error) {
          reason = resData.error + (resData.details ? `: ${resData.details}` : '');
        } else if (resData?.textFallback) {
          reason = resData.textFallback;
        }
        throw new Error(reason);
      }
      const hora = resData.ts ? new Date(resData.ts).toLocaleTimeString('es-CL', { hour12: false }) : '';
      const distancia = typeof resData.distanceMeters === 'number' ? `${resData.distanceMeters} metros` : '';
      displayAlert(alertSuccess, `Marcación registrada correctamente${hora ? ' a las ' + hora :
                                          ''}${distancia ? ' a ' + distancia : ''}. ✅`, 3000);
      setTimeout(() => navigateTo('/private/employees/dashboard'), 1500);
    } catch (e) {
      displayAlert(alertError, 'Error en marcación: ' + (e?.message || 'desconocido'), 3000);
      await syncAttendanceButtons();
    }
}

function setButtonsState(inStatus, outStatus) {
    const btnIn = qs('#att-in');
    const btnOut = qs('#att-out');

    if (btnIn) {
      if(inStatus === null) {
        btnIn.classList.add('hidden');
        btnIn.hidden = true;
      } else {
        btnIn.classList.remove('hidden');
        btnIn.hidden = false;
        btnIn.disabled = !inStatus;
      }
    }

    if (btnOut) {
      if(outStatus === null) {
        btnOut.classList.add('hidden');
        btnOut.hidden = true;
      } else {
        btnOut.classList.remove('hidden');
        btnOut.hidden = false;
        btnOut.disabled = !outStatus;
      }
    }
}

// Inicializar botones
function bindEvents() {
    const btnIn = qs('#att-in');
    const btnOut = qs('#att-out');
    if(btnIn){btnIn.addEventListener('click', () => punch('IN'));}
    if(btnOut){btnOut.addEventListener('click', () => punch('OUT'));}
}

//Funciones de utilidad operativa
function getDistanceMeters(lat1, lon1, lat2, lon2) {
  // Fórmula Haversine
  const R = 6371000; // Radio tierra en metros
  const toRad = x => x * Math.PI / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

function getNearestSite(userLat, userLon, sites) {
  let nearest = null;
  let minDistance = Infinity;
  for (const site of sites) {
    if (typeof site.lat !== 'number' || typeof site.lon !== 'number') continue;
    const d = getDistanceMeters(userLat, userLon, site.lat, site.lon);
    if (d < minDistance) {
      minDistance = d;
      nearest = { ...site, distance: d };
    }
  }
  return nearest;
}

const getCurrentPosition = () => new Promise((resolve, reject) => {
  if (!navigator.geolocation) return reject(new Error('Geolocalización no soportada'));
  navigator.geolocation.getCurrentPosition(
    (p) => resolve(p),
    (e) => reject(new Error(e.message || 'No se pudo obtener ubicación')),
    { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
  );
});