import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// ========== CONFIGURABLE ==========
const MAX_DISTANCE_METERS = 35; // Cambia aquí la distancia máxima permitida

// Helper para calcular distancia Haversine en metros
function getDistanceMeters(lat1, lon1, lat2, lon2) {
  const R = 6371000;
  const toRad = x => x * Math.PI / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

// Encuentra el sitio más cercano y su distancia
function getNearestSite(userLat, userLon, sites) {
  let nearest = null;
  let minDistance = Infinity;
  for (const site of sites) {
    if (typeof site.latitude !== 'number' || typeof site.longitude !== 'number') continue;
    const d = getDistanceMeters(userLat, userLon, site.latitude, site.longitude);
    if (d < minDistance) {
      minDistance = d;
      nearest = { ...site, distance: d };
    }
  }
  return nearest;
}

// Helper para querySelector
const qs = (s) => document.querySelector(s);

// UI elements
const form = qs('#visit-widget');
const statusDiv = qs('#visit-status');
const siteInfoDiv = qs('#visit-site-info');
const mapContainer = qs('#visit-map-container');
const mapDiv = qs('#visit-map');
const btnSave = qs('#save-visit-btn');
const btnCancel = qs('#cancel-visit-btn');
const photoInput = qs('#visit-photo');
const photoPreview = qs('#visit-photo-preview');
const videoInput = qs('#visit-video');
const videoPreview = qs('#visit-video-preview');
const descriptionInput = qs('#visit-description');

// State
let map, marker, closestSite = null, visitLat = null, visitLon = null, currentDistance = null;
let supervisorSites = window.siteList || []; // Asume que window.siteList tiene los sitios
let noSiteTimeout = null; // <-- para redirección si no hay sitio

// Mensajería UI
function showStatus(msg, color = "#444") {
  statusDiv.textContent = msg || '';
  statusDiv.style.color = color;
}
function showSiteInfo(msg, color = "#0077b6") {
  siteInfoDiv.textContent = msg || '';
  siteInfoDiv.style.color = color;
}

// Vista previa de foto/video
photoInput?.addEventListener('change', () => {
  photoPreview.innerHTML = '';
  const file = photoInput.files && photoInput.files[0];
  if (file) {
    const url = URL.createObjectURL(file);
    const img = document.createElement('img');
    img.src = url;
    img.style.maxWidth = "180px";
    img.style.borderRadius = "6px";
    photoPreview.appendChild(img);
  }
});
videoInput?.addEventListener('change', () => {
  videoPreview.innerHTML = '';
  const file = videoInput.files && videoInput.files[0];
  if (file) {
    const url = URL.createObjectURL(file);
    const video = document.createElement('video');
    video.src = url;
    video.controls = true;
    video.style.maxWidth = "220px";
    video.style.borderRadius = "6px";
    videoPreview.appendChild(video);
  }
});

// Inicializa el mapa (Google Maps)
function initMap(lat = -33.45, lon = -70.65, zoom = 16) {
  if (!mapDiv || !window.google || !google.maps) return;

  map = new google.maps.Map(mapDiv, {
    center: { lat, lng: lon },
    zoom: zoom,
    mapTypeId: 'roadmap',
    disableDefaultUI: true
  });

  marker = new google.maps.Marker({
    map: map,
    position: { lat, lng: lon },
    draggable: true,
    title: "Tu ubicación"
  });

  marker.addListener('dragend', (e) => {
    visitLat = e.latLng.lat();
    visitLon = e.latLng.lng();
    updateNearestSite();
  });
}

// Obtiene la ubicación actual del navegador
function setCurrentLocation() {
  if (!navigator.geolocation) {
    showStatus("Tu navegador no soporta geolocalización.", "#b91c1c");
    return;
  }
  showStatus("Obteniendo ubicación actual...", "#0077b6");
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      visitLat = pos.coords.latitude;
      visitLon = pos.coords.longitude;
      map.setCenter({ lat: visitLat, lng: visitLon });
      marker.setPosition({ lat: visitLat, lng: visitLon });
      showStatus("Ubicación detectada, buscando sitio más cercano...", "#15803d");
      updateNearestSite();
    },
    (err) => {
      showStatus("No se pudo obtener la ubicación actual: " + (err.message || err), "#b91c1c");
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

// Determina el sitio más cercano y actualiza la UI
function updateNearestSite() {
  if (!supervisorSites || !visitLat || !visitLon) return;
  const nearest = getNearestSite(visitLat, visitLon, supervisorSites);
  closestSite = nearest;
  currentDistance = nearest ? nearest.distance : null;

  // Cancelar timeout previo si existe
  if (noSiteTimeout) {
    clearTimeout(noSiteTimeout);
    noSiteTimeout = null;
  }

  if (nearest) {
    if (nearest.distance <= MAX_DISTANCE_METERS) {
      showStatus(`Estás en el sitio "${nearest.name}". Puedes registrar visita aquí.`, "#059669");
      showSiteInfo(`Sitio: ${nearest.name} (${nearest.address || ''})`, "#059669");
      btnSave.disabled = false;
    } else {
      showStatus(`El sitio más cercano es "${nearest.name}" a ${nearest.distance.toFixed(1)} metros. Acércate para registrar.`, "#d97706");
      showSiteInfo("", "#d97706");
      btnSave.disabled = true;
    }
  } else {
    showStatus("No se encontró ningún sitio cercano. Redirigiendo...", "#b00020");
    showSiteInfo("", "#b00020");
    btnSave.disabled = true;

    // Espera 3 segundos y redirige al dashboard
    noSiteTimeout = setTimeout(() => {
      navigateTo("private/supervisors/dashboard");
    }, 3000);
  }
}

// Enviar formulario (registro de visita)
form?.addEventListener('submit', async (e) => {
  e.preventDefault();

  if (!visitLat || !visitLon) {
    showStatus("No se ha detectado ubicación válida.", "#b91c1c");
    return;
  }
  if (!closestSite) {
    showStatus("No se ha detectado sitio cercano.", "#b91c1c");
    return;
  }
  if (typeof currentDistance !== 'number' || currentDistance > MAX_DISTANCE_METERS) {
    showStatus(`Debes estar a menos de ${MAX_DISTANCE_METERS} metros del sitio para registrar visita.`, "#b91c1c");
    return;
  }

  showStatus("Registrando visita...", "#0077b6");
  btnSave.disabled = true;

  const formData = new FormData();
  formData.append('siteId', closestSite.id);
  formData.append('latitude', visitLat);
  formData.append('longitude', visitLon);
  formData.append('description', descriptionInput.value);

  if (photoInput.files && photoInput.files[0]) {
    formData.append('photo', photoInput.files[0]);
  }
  if (videoInput.files && videoInput.files[0]) {
    formData.append('video', videoInput.files[0]);
  }

  try {
    const endpoint = form.dataset.visitEndpoint || '/api/supervisor-visits/register';
    const res = await fetchWithAuth(endpoint, {
      method: 'POST',
      body: formData,
      credentials: 'same-origin'
    });
    if (!res.ok) {
      let out = await res.json().catch(() => ({}));
      throw new Error(out.error || "Error al registrar visita.");
    }
    showStatus("✅ Visita registrada correctamente.", "#15803d");
    form.reset();
    photoPreview.innerHTML = '';
    videoPreview.innerHTML = '';
    btnSave.disabled = true;
  } catch (err) {
    showStatus("No se pudo registrar la visita: " + (err.message || err), "#b91c1c");
    btnSave.disabled = false;
  }
});

// Botón cancelar
btnCancel?.addEventListener('click', (e) => {
  e.preventDefault();
  form.reset();
  photoPreview.innerHTML = '';
  videoPreview.innerHTML = '';
  showStatus("Registro cancelado.", "#b91c1c");
  btnSave.disabled = true;
});

// Inicialización automática cuando Google Maps esté listo
function waitGoogleMapsAndInit(retry = 0) {
  if (window.google && window.google.maps) {
    // Puedes obtener una ubicación inicial, por defecto Santiago
    let latInit = -33.45, lonInit = -70.65;
    if (supervisorSites.length && typeof supervisorSites[0].latitude === "number" && typeof supervisorSites[0].longitude === "number") {
      latInit = supervisorSites[0].latitude;
      lonInit = supervisorSites[0].longitude;
    }
    initMap(latInit, lonInit);
    setCurrentLocation();
  } else if (retry < 15) {
    setTimeout(() => waitGoogleMapsAndInit(retry + 1), 250 + 100 * retry);
  } else {
    showStatus('No se pudo cargar el mapa de Google.', "#b91c1c");
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", waitGoogleMapsAndInit);
} else {
  waitGoogleMapsAndInit();
}