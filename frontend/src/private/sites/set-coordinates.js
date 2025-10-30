// Archivo: /js/private/sites/set-coordinates.js

// Asume que Google Maps API ya está cargado en la página
// El formulario espera: #site-coordinates-form, campos #site-select, #site-latitude, #site-longitude
// y un mapa en #site-map. El botón "Usar mi ubicación actual": #set-current-location

const LAT_INPUT_ID = 'site-latitude';
const LON_INPUT_ID = 'site-longitude';
const SITE_SELECT_ID = 'site-select';
const MAP_CONTAINER_ID = 'site-map';
const FORM_ID = 'site-coordinates-form';
const BTN_GPS_ID = 'set-current-location';
const ERROR_ID = 'site-coordinates-error';
const OK_ID = 'site-coordinates-ok';

let map, marker;

function setLatLonFields(lat, lon) {
  document.getElementById(LAT_INPUT_ID).value = lat.toFixed(7);
  document.getElementById(LON_INPUT_ID).value = lon.toFixed(7);
}

function setMarkerPosition(lat, lon) {
  if (marker) {
    marker.setPosition({ lat, lng: lon });
    map.setCenter({ lat, lng: lon });
  } else {
    marker = new google.maps.Marker({
      position: { lat, lng: lon },
      map: map,
      draggable: true,
      title: 'Punto de referencia',
      icon: {
        url: "https://maps.gstatic.com/mapfiles/ms2/micons/red-dot.png"
      }
    });
    marker.addListener('dragend', (e) => {
      setLatLonFields(e.latLng.lat(), e.latLng.lng());
    });
  }
}

function showError(msg) {
  const err = document.getElementById(ERROR_ID);
  if (err) err.textContent = msg || "";
  const ok = document.getElementById(OK_ID);
  if (ok) ok.style.display = "none";
}

function showOk(msg) {
  const ok = document.getElementById(OK_ID);
  if (ok) {
    ok.textContent = msg || "Coordenadas guardadas ✅";
    ok.style.display = "";
  }
  showError("");
}

function initMap(latInit = -33.45, lonInit = -70.65, zoom = 16) {
  const mapDiv = document.getElementById(MAP_CONTAINER_ID);
  if (!mapDiv || !window.google || !google.maps) return;

  map = new google.maps.Map(mapDiv, {
    center: { lat: latInit, lng: lonInit },
    zoom: zoom,
    mapTypeId: 'roadmap',
    disableDefaultUI: true
  });

  // Click en mapa para fijar punto
  map.addListener('click', (e) => {
    setLatLonFields(e.latLng.lat(), e.latLng.lng());
    setMarkerPosition(e.latLng.lat(), e.latLng.lng());
  });
}

function setCurrentLocation() {
  if (!navigator.geolocation) {
    showError("Tu navegador no soporta geolocalización.");
    return;
  }
  showError("Obteniendo ubicación actual...");
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;
      setLatLonFields(lat, lon);
      setMarkerPosition(lat, lon);
      map.setCenter({ lat, lng: lon });
      showError("");
    },
    (err) => {
      showError("No se pudo obtener la ubicación actual: " + (err.message || err));
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

function fillFieldsFromSite(site) {
  // Si site tiene lat/lon, usa esos valores
  if (site && typeof site.latitude === "number" && typeof site.longitude === "number") {
    setLatLonFields(site.latitude, site.longitude);
    setMarkerPosition(site.latitude, site.longitude);
    map.setCenter({ lat: site.latitude, lng: site.longitude });
  }
}

async function postCoordinates(form, endpoint) {
  const formData = new FormData(form);
  const body = {
    siteId: formData.get('siteId'),
    latitude: formData.get('latitude'),
    longitude: formData.get('longitude')
  };

  if (!body.siteId || !body.latitude || !body.longitude) {
    showError("Completa todos los campos requeridos.");
    return;
  }

  try {
    showError("Guardando...");
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(body),
      credentials: 'same-origin'
    });
    if (!res.ok) {
      let out = await res.json().catch(() => ({}));
      throw new Error(out.error || "Error al guardar coordenadas.");
    }
    showOk("Coordenadas guardadas ✅");
  } catch (e) {
    showError("No se pudo guardar: " + (e.message || e));
  }
}

function onSiteChange(sites) {
  // Si quieres cargar la lat/lon del site seleccionado (si existe), puedes hacerlo aquí
  const select = document.getElementById(SITE_SELECT_ID);
  if (!select) return;
  select.addEventListener('change', () => {
    const siteId = select.value;
    if (!siteId) return;
    const found = sites.find(s => String(s.id) === String(siteId));
    if (found && typeof found.latitude === "number" && typeof found.longitude === "number") {
      fillFieldsFromSite(found);
    }
  });
}

// Espera a que Google Maps esté cargado y el DOM listo
async function initSiteCoordinatesFragment() {
  // Si tienes sites con lat/lon, pásalos aquí
  // Ejemplo: window.siteList = [{id, name, latitude, longitude}, ...]
  const sites = window.siteList || [];
  const select = document.getElementById(SITE_SELECT_ID);

  // Centra el mapa en el primer sitio con coordenadas, o en Santiago por defecto
  let latInit = -33.45, lonInit = -70.65;
  if (sites.length && typeof sites[0].latitude === "number" && typeof sites[0].longitude === "number") {
    latInit = sites[0].latitude;
    lonInit = sites[0].longitude;
  }
  initMap(latInit, lonInit);

  document.getElementById(BTN_GPS_ID)?.addEventListener('click', setCurrentLocation);

  // Al cambiar el sitio, actualizar el marcador si hay coordenadas
  if (sites.length && select) onSiteChange(sites);

  // Al enviar el form, POST coordenadas
  const form = document.getElementById(FORM_ID);
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const endpoint = form.dataset.endpoint || '/api/sites/set-coordinates';
      postCoordinates(form, endpoint);
    });
  }
}

// Reintenta si Google Maps aún no está listo
function waitGoogleMapsAndInit(retry = 0) {
  if (window.google && window.google.maps) {
    initSiteCoordinatesFragment();
  } else if (retry < 10) {
    setTimeout(() => waitGoogleMapsAndInit(retry + 1), 300 + 150 * retry);
  } else {
    showError('No se pudo cargar el mapa de Google.');
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => waitGoogleMapsAndInit());
} else {
  waitGoogleMapsAndInit();
}

document.addEventListener('content:loaded', () => waitGoogleMapsAndInit());