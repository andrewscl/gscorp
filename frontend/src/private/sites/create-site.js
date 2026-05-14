import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js'; 
import { enableMarkerDrag } from '../../shared/maps/enable-marker-drag.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-cancel');

let mainMarker = null;

async function startCreateMap() {
  const apiKey = googleMapsConfig.apiKey;

  console.log('Loading Google Maps API...');
  await loadGoogleMapsAPI(apiKey);
  const map = await initMap('map', {
    mapTypeId: 'hybrid',
    zoom: 10,
  });

  map.addListener('click', async (event) => {
    const lat = typeof event.latLng.lat === 'function' ? event.latLng.lat() : event.latLng.lat;
    const lon = typeof event.latLng.lng === 'function' ? event.latLng.lng() : event.latLng.lng;

    const inputLat = qs('#siteLat');
    const inputLon = qs('#siteLon');

    if(!mainMarker){
      mainMarker = await addAdvancedMarker(
                                map, 'Nuevo sitio', lat, lon);

      enableMarkerDrag(mainMarker, (newPos) => {
        const newLat = typeof newPos.lat === 'function' ? newPos.lat() : newPos.lat;
        const newLon = typeof newPos.lng === 'function' ? newPos.lng() : newPos.lng;
        if(inputLat) inputLat.value = newLat;
        if(inputLon) inputLon.value = newLon;
      });

    } else {
      mainMarker.position = { lat, lng: lon };
    }

    // Actualizar inputs tras el click
      if(inputLat) inputLat.value = lat;
      if(inputLon) inputLon.value = lon;
  });
}

async function onSubmitCreate(e) {
  e.preventDefault();

  const projectId = Number(qs('#siteProjectId')?.value);
  const name     = qs('#siteName')?.value?.trim();
  const code     = qs('#siteCode')?.value?.trim() || null;
  const address  = qs('#siteAddress')?.value?.trim() || null;
  const latStr   = qs('#siteLat')?.value?.trim();
  const lonStr   = qs('#siteLon')?.value?.trim();
  const timeZone = qs('#siteTz')?.value?.trim() || null;
  const active   = !!qs('#siteActive')?.checked;

  // Parseo seguro de coordenadas (null si vacío)
  const lat = latStr ? Number(latStr) : null;
  const lon = lonStr ? Number(lonStr) : null;

  // Validaciones mínimas
  if (!projectId || projectId === 0) {
    displayAlert(alertError, 'El proyecto es obligatorio.', 1500);
    return;
  }
  if (!name) {
    displayAlert(alertError, 'El nombre es obligatorio.', 1500);
    return;
  }
  if (lat === null || lon === null) {
    displayAlert(alertError, 'Las coordenadas son obligatorias. Haz click en el mapa para seleccionarlas.', 1500);
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createSiteForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/sites/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        projectId,
        name,
        address,
        lat,
        lon,
        timeZone,
        active
      })
    });
    if (!res.ok) {
      displayAlert(alertError, 'El sitio no pudo ser creado.', 1500);
    }
    displayAlert(alertSuccess, 'El sitio ha sido creado exitosamente.', 1500);
    setTimeout(() => { navigateTo('/private/sites/table-view'); }, 1500);

  } catch (e2) {
    err && (err.textContent = e2.message || 'No se pudo crear el sitio');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}


const cancelCreateSite = () => {
    displayAlert(alertCancel, 'La creación del sitio a sido cancelada.', 2000);
    setTimeout(() => navigateTo('/private/sites/table-view', true), 2000);
}


function bindCreateSite() {
    const createBtn = qs('#submit');
    if (createBtn) {
        createBtn.addEventListener('click', onSubmitCreate);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateSite);
    }
  }


(function init() {
  bindCreateSite();

  startCreateMap();

})();