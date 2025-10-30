import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const LAT_INPUT_ID = 'site-latitude';
const LON_INPUT_ID = 'site-longitude';
const SITE_SELECT_ID = 'site-select';
const MAP_CONTAINER_ID = 'site-map';
const FORM_ID = 'site-coordinates-form';
const BTN_GPS_ID = 'set-current-location';
const BTN_CANCEL_ID = 'cancel-set-coordinates';
const ERROR_ID = 'site-coordinates-error';
const OK_ID = 'site-coordinates-ok';

let map, marker;

function setLatLonFields(lat, lon) {
  qs(`#${LAT_INPUT_ID}`).value = lat.toFixed(7);
  qs(`#${LON_INPUT_ID}`).value = lon.toFixed(7);
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
  const err = qs(`#${ERROR_ID}`);
  if (err) err.textContent = msg || "";
  const ok = qs(`#${OK_ID}`);
  if (ok) ok.style.display = "none";
}

function showOk(msg) {
  const ok = qs(`#${OK_ID}`);
  if (ok) {
    ok.textContent = msg || "Coordenadas guardadas ✅";
    ok.style.display = "";
  }
  showError("");
}

function initMap(latInit = -33.45, lonInit = -70.65, zoom = 16) {
  const mapDiv = qs(`#${MAP_CONTAINER_ID}`);
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
    const res = await fetchWithAuth(endpoint, {
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
    setTimeout(() => {
      // Opcional: navega al listado de sitios o cierra el fragmento/modal
      // navigateTo('/private/sites/table-view');
      // O simplemente limpia el form:
      resetFormAndMap();
    }, 800);
  } catch (e) {
    showError("No se pudo guardar: " + (e.message || e));
  }
}

function onSiteChange(sites) {
  const select = qs(`#${SITE_SELECT_ID}`);
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

function resetFormAndMap() {
  qs(`#${LAT_INPUT_ID}`).value = '';
  qs(`#${LON_INPUT_ID}`).value = '';
  if (marker) {
    marker.setMap(null);
    marker = null;
  }
  if (map) {
    map.setCenter({ lat: -33.45, lng: -70.65 });
  }
  showError("");
  const ok = qs(`#${OK_ID}`);
  if (ok) ok.style.display = "none";
}

async function initSiteCoordinatesFragment() {
  const sites = window.siteList || [];
  const select = qs(`#${SITE_SELECT_ID}`);

  let latInit = -33.45, lonInit = -70.65;
  if (sites.length && typeof sites[0].latitude === "number" && typeof sites[0].longitude === "number") {
    latInit = sites[0].latitude;
    lonInit = sites[0].longitude;
  }
  initMap(latInit, lonInit);

  qs(`#${BTN_GPS_ID}`)?.addEventListener('click', setCurrentLocation);

  if (sites.length && select) onSiteChange(sites);

  const form = qs(`#${FORM_ID}`);
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const endpoint = form.dataset.endpoint || '/api/sites/set-coordinates';
      postCoordinates(form, endpoint);
    });
  }

  // Botón cancelar: limpia el formulario y resetea el mapa
  const btnCancel = qs(`#${BTN_CANCEL_ID}`);
  if (btnCancel) {
    btnCancel.addEventListener('click', (e) => {
      e.preventDefault();
      resetFormAndMap();
      // Opcional: navega al listado o cierra modal/fragment
      // navigateTo('/private/sites/table-view');
    });
  }
}

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