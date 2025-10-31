import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// ========== CONFIGURABLE ==========
const MAX_DISTANCE_METERS = 35;

// --- Estado / referencias ---
let supervisorSites = [];
let noSiteTimeout = null;

// --- Utilidades ---
function getDistanceMeters(lat1, lon1, lat2, lon2) {
  const R = 6371000;
  const toRad = x => x * Math.PI / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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

// --- Cargar sitios desde API ---
async function loadSites() {
  try {
    const res = await fetchWithAuth('/api/site-supervision-visits/sites', { credentials: 'same-origin' });
    if (!res.ok) throw new Error(`Error cargando sitios: ${res.status}`);
    supervisorSites = await res.json();
  } catch (e) {
    console.error("No se pudo cargar la lista de sitios:", e);
    supervisorSites = [];
  }
}

// --- Mostrar estado y lógica de registro ---
async function showCurrentSiteStatus() {
  const statusEl = document.getElementById('visit-status');
  const infoEl = document.getElementById('visit-site-info');
  const btnSave = document.getElementById('save-visit-btn');

  if (!navigator.geolocation) {
    statusEl.textContent = "Geolocalización no soportada";
    infoEl.textContent = "";
    btnSave.disabled = true;
    return;
  }
  statusEl.textContent = "Detectando instalación más cercana...";
  infoEl.textContent = "";

  try {
    const pos = await new Promise((resolve, reject) =>
      navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 15000 })
    );

    if (!supervisorSites.length) {
      statusEl.textContent = "No hay sitios configurados.";
      statusEl.style.color = "#b00020";
      infoEl.textContent = "";
      btnSave.disabled = true;
      // Redirección después de 3 segundos (opcional)
      if (noSiteTimeout) clearTimeout(noSiteTimeout);
      noSiteTimeout = setTimeout(() => navigateTo("/private/supervisors/dashboard"), 3000);
      return;
    }

    const nearestSite = getNearestSite(pos.coords.latitude, pos.coords.longitude, supervisorSites);

    // Cancelar timeout previo
    if (noSiteTimeout) {
      clearTimeout(noSiteTimeout);
      noSiteTimeout = null;
    }

    if (nearestSite && nearestSite.distance <= MAX_DISTANCE_METERS) {
      statusEl.textContent = `Estás en el sitio "${nearestSite.name}". Puedes registrar visita aquí.`;
      statusEl.style.color = "#059669";
      infoEl.textContent = `Sitio: ${nearestSite.name} (${nearestSite.address || ''})`;
      infoEl.style.color = "#059669";
      btnSave.disabled = false;
      btnSave.dataset.siteId = nearestSite.id;
      btnSave.dataset.latitude = pos.coords.latitude;
      btnSave.dataset.longitude = pos.coords.longitude;
    } else if (nearestSite) {
      statusEl.textContent = `El sitio más cercano es "${nearestSite.name}" a ${nearestSite.distance.toFixed(1)} metros. Acércate para registrar.`;
      statusEl.style.color = "#d97706";
      infoEl.textContent = "";
      infoEl.style.color = "#d97706";
      btnSave.disabled = true;
    } else {
      statusEl.textContent = "No se encontró ningún sitio cercano.";
      statusEl.style.color = "#b00020";
      infoEl.textContent = "";
      infoEl.style.color = "#b00020";
      btnSave.disabled = true;
      // Redirección después de 3 segundos
      noSiteTimeout = setTimeout(() => navigateTo("/private/supervisors/dashboard"), 3000);
    }
  } catch (e) {
    const statusEl = document.getElementById('visit-status');
    const infoEl = document.getElementById('visit-site-info');
    statusEl.textContent = "No se pudo obtener ubicación.";
    statusEl.style.color = "#b00020";
    infoEl.textContent = "";
    infoEl.style.color = "#b00020";
    const btnSave = document.getElementById('save-visit-btn');
    if (btnSave) btnSave.disabled = true;
  }
}

// --- Inicializar widget ---
function initSupervisorVisitWidget() {
  const root = document.getElementById('visit-widget');
  if (!root || root.dataset.svInit === '1') return;
  root.dataset.svInit = '1';

  const btnSave = document.getElementById('save-visit-btn');
  const btnCancel = document.getElementById('cancel-visit-btn');
  const descriptionInput = document.getElementById('visit-description');
  const photoInput = document.getElementById('visit-photo');
  const photoPreview = document.getElementById('visit-photo-preview');
  const videoInput = document.getElementById('visit-video');
  const videoPreview = document.getElementById('visit-video-preview');

  // Previsualización foto/video
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

  // Registrar visita (NO submit de form, solo botón)
  btnSave?.addEventListener('click', async (e) => {
    e.preventDefault();

    const siteId = btnSave.dataset.siteId;
    const latitude = btnSave.dataset.latitude;
    const longitude = btnSave.dataset.longitude;
    const description = descriptionInput.value;

    if (!siteId || !latitude || !longitude) {
      showCurrentSiteStatus(); // Refresca estado, por seguridad
      return;
    }

    btnSave.disabled = true;
    showStatus("Registrando visita...", "#0077b6");

    const payload = {
      siteId,
      latitude,
      longitude,
      description
    };

    try {
      const endpoint = root.dataset.visitEndpoint || '/api/site-supervision-visits/create';
      const res = await fetchWithAuth(endpoint, {
        method: 'POST',
        body: JSON.stringify(payload),
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        credentials: 'same-origin'
      });
      if (!res.ok) {
        let out = await res.json().catch(() => ({}));
        throw new Error(out.error || "Error al registrar visita.");
      }
      showStatus("✅ Visita registrada correctamente.", "#15803d");
      root.reset();
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
    root.reset();
    photoPreview.innerHTML = '';
    videoPreview.innerHTML = '';
    showStatus("Registro cancelado.", "#b91c1c");
    btnSave.disabled = true;
    navigateTo("/private/supervisors/dashboard")
  });

  // Inicializar mapa
  showCurrentLocationOnMap();

  // Mostrar estado al cargar el widget
  showCurrentSiteStatus();
}

// --- Mapita ---
function showCurrentLocationOnMap() {
  const mapDiv = document.getElementById('visit-map');
  if (!mapDiv) return;

  if (mapDiv._google_map) {
    mapDiv._google_map = null;
    mapDiv.innerHTML = "";
  }

  if (!navigator.geolocation) {
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>Geolocalización no soportada</div>";
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;
      if (window.google && window.google.maps) {
        const mapInstance = new google.maps.Map(mapDiv, {
          center: { lat, lng: lon },
          zoom: 17,
          mapTypeId: 'roadmap',
          disableDefaultUI: true
        });
        new google.maps.Marker({ position: { lat, lng: lon }, map: mapInstance });
        mapDiv._google_map = mapInstance;
      } else {
        mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>Google Maps no cargado</div>";
      }
    },
    (err) => {
      mapDiv.innerHTML = `<div style='padding:1em;text-align:center;color:#b00020'>No se pudo obtener ubicación.<br>${err.message}</div>`;
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

// --- Helpers de UI ---
function showStatus(msg, color = "#444") {
  const statusDiv = document.getElementById('visit-status');
  if (!statusDiv) return;
  statusDiv.textContent = msg || '';
  statusDiv.style.color = color;
}

// --- Auto-init ---
async function supervisorVisitWidgetInitAll() {
  await loadSites();
  initSupervisorVisitWidget();
  await showCurrentSiteStatus();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', supervisorVisitWidgetInitAll);
} else {
  supervisorVisitWidgetInitAll();
}
document.addEventListener('content:loaded', supervisorVisitWidgetInitAll);

// --- Exponer helpers si necesitas testear ---
export {
  getDistanceMeters,
  getNearestSite,
  showCurrentSiteStatus,
  initSupervisorVisitWidget
};