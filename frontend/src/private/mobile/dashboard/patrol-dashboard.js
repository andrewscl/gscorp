import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import loadGoogleMapsAPI from '../../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../../shared/maps/advanced-marker.js';
import { displayAlert } from '../../../shared/display-alert.js';
import { getNearestSite } from '../../../shared/maps/map-utils.js';

let googleMapInstance = null;
let userMarkerInstance = null;
const MAX_DISTANCE_GEOFENCE = 35;

const qs = (s) => document.querySelector(s);
const qa = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');


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
                                          Puedes comenzar un patrullaje desde aquí.`, 3000);

      if(nearestSite){
        await startViewMap(nearestSite);
        await loadScheduledPatrols(nearestSite.externalId);
      }

      } else if (nearestSite) {
        displayAlert(alertInfo, `El sitio más cercano es "${nearestSite.name}" a
            ${nearestSite.distance.toFixed(1)} metros. Acércate para acceder a las opciones.`, 3000);

        if(nearestSite){
        await startViewMap(nearestSite);
        }

        setTimeout(() => navigateTo('/private/employees/dashboard'), 3000);
      } else {
        displayAlert(alertError, 'No se encontró ningún sitio cercano.', 5000);
      }
  } catch (e) {
    displayAlert(alertError, 'No fue posible obtener la ubicación: '
                      + (e.message || 'Tiempo de espera agotado.'), 5000);
  }
}


const startViewMap = async (nearestSite) => {
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
    console.error('[patrol-dashboard.js] Error al cargar la API de Google Maps:'
                                                                    , error);
  }
};

let sitesList = [];
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


let patrolSchedulesList = [];
const loadScheduledPatrols = async (siteExternalId) => {
  const container = qs('#patrolSchedulesContainer')
  try {
    const response = await fetchWithAuth(`/api/patrol-schedules/next-24h-site-patrol-schedules/${siteExternalId}`, {
       credentials: 'same-origin'
    });

    if (!response.ok) {
      throw new Error(`Error cargando schedules: ${response.status}`);
    }

    patrolSchedulesList = await response.json();

    renderScheduledCards(patrolSchedulesList, container);

  } catch (e) {

      console.error("No se pudo cargar la lista de sitios:", e);
      patrolSchedulesList = [];

      if (TableBody) {
              TableBody.innerHTML = `
                <div class="text-center text-danger small p-3">
                     ❌ Error al cargar la agenda de rondas para las próximas 24 horas.
                </div>`;
      }
  }
};


const renderScheduledCards = (schedules, container) => {
    if (!container) return;

    if (!schedules || schedules.length === 0) {
        container.innerHTML = `
            <div class="patrol-loader-placeholder text-muted small">
                📅 No tienes rondas programadas para hoy en este turno.
            </div>`;
        return;
    }

    container.innerHTML = schedules.map((sch, index) => {
        const timeFormatted = sch.startTime ? sch.startTime.substring(0, 5) : '--:--';
        const badgeClasses = ['is-scheduled', 'is-free', 'is-supervision'];
        const currentBadgeClass = badgeClasses[index % badgeClasses.length];

    return `
        <div class="patrol-card-item patrol-action-card" data-url="/private/patrol-executions/schedule-execute/${sch.externalId}/">
            <div class="patrol-card-item__icon-box ${currentBadgeClass}">
                <i class="bi bi-journal-check"></i>
            </div>
            <div class="patrol-card-item__body">
                <div class="card-main-info">
                    <h5>${sch.patrolName}</h5> 
                    <span class="time-tag">${timeFormatted}</span>
                </div>
                <p class="text-muted small">Ronda Planificada • Estado: ${sch.status === 'SCHEDULED' ? 'Pendiente' : sch.status}</p>
            </div>
            <div class="patrol-card-item__arrow">
                <i class="bi bi-chevron-right"></i>
            </div>
        </div>
    `;
    }).join('');

    // Asociación de navegación
    if(container) {
      container.querySelectorAll('.patrol-action-card').forEach(card => {
        card.addEventListener('click', (e) => {
          const targetUrl = e.currentTarget.getAttribute('data-url');
          displayAlert(alertSuccess, 'Abriendo bitácora de ronda...', 1500);
          setTimeout(() => navigateTo(targetUrl, 1500));
        });
      });
    }

}; 



const backToEmployeeDashboard = () => {
    setTimeout(() => navigateTo('/private/employees/dashboard', true), 1000);
};


function bindEvents() {

    const backToEmployeeDashboardBtn = qs('#backToEmployeeDashboard');
    if (backToEmployeeDashboardBtn) {
        backToEmployeeDashboardBtn.addEventListener('click', backToEmployeeDashboard);
    }
}

async function initComponent() {
    
    displayAlert(alertInfo,
        'Conectando con el servicio de Georreferenciación...', 1500);

    try {
        await loadSites();

        await startViewMap(null);

        await defineCurrentPosition();

    } catch (e) {
        console.error('[patrol-dashboard] initComponent failed', e); 
    }

}

(async function init() {
  bindEvents();
  await initComponent();
  console.log('View patrol-dashboard page initialized.');
})();