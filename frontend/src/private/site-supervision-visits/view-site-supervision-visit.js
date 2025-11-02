// view-site-supervision-visit.js

import { navigateTo } from '../../navigation-handler.js';

// Mapa de solo visualización con Google Maps
function showVisitLocationOnMap() {
  const mapDiv = document.getElementById('visitMap');
  const latInput = document.getElementById('visitLat');
  const lonInput = document.getElementById('visitLon');

  if (!mapDiv || !latInput || !lonInput) return;

  const lat = parseFloat(latInput.value);
  const lon = parseFloat(lonInput.value);

  if (!lat || !lon || isNaN(lat) || isNaN(lon)) {
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>No hay coordenadas para mostrar el mapa.</div>";
    return;
  }

  // Google Maps API debe estar cargada en la página (de lo contrario, mostrar error)
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
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#b00020'>Google Maps no cargado</div>";
  }
}

// Acción para volver al listado usando SPA navigation (si aplica)
const volverBtn = document.querySelector('.actions .vs-btn.vs-secondary[data-path]');
if (volverBtn) {
  volverBtn.addEventListener('click', (e) => {
    e.preventDefault();
    const path = volverBtn.getAttribute('data-path');
    if (path) navigateTo(path);
  });
}

// Inicialización
document.addEventListener('DOMContentLoaded', showVisitLocationOnMap);
// Si tienes recarga parcial con eventos personalizados, escucha también
document.addEventListener('content:loaded', showVisitLocationOnMap);
