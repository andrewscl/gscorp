import { navigateTo } from '../../navigation-handler.js';

// Guarda los datos necesarios para inicializar el mapa cuando Google Maps esté listo
window._pendingMapInit = null;

/**
 * Inicializa la vista del sitio y prepara los datos para el mapa.
 */
export function init({ container, path }) {
  console.log('View-site.js activado');
  // Botón "Volver al listado"
  const backBtn = container.querySelector('.vs-btn.vs-secondary[data-path]');
  if (backBtn) {
    backBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const path = backBtn.getAttribute('data-path');
      if (path) navigateTo(path);
    });
  }

  // Preparar datos para el mapa
  const latInput = container.querySelector('#siteLat');
  const lonInput = container.querySelector('#siteLon');
  const mapDiv = container.querySelector('#siteMap');
  window._pendingMapInit = { latInput, lonInput, mapDiv };

  // Si Google Maps ya está cargado, inicializa el mapa
  if (window.google && window.google.maps) {
    window.onGoogleMapsReady();
  }
}

/**
 * Esta función será llamada por el callback de Google Maps cuando la API esté lista.
 * Inicializa el mapa utilizando los datos guardados en _pendingMapInit.
 */
window.onGoogleMapsReady = function() {
  const pending = window._pendingMapInit;
  if (!pending) return;

  const { latInput, lonInput, mapDiv } = pending;
  if (mapDiv && latInput && lonInput) {
    if (mapDiv._google_map) return; // Evita doble inicialización

    const lat = parseFloat(latInput.value) || -33.45;
    const lon = parseFloat(lonInput.value) || -70.66;
    const map = new google.maps.Map(mapDiv, {
      center: { lat, lng: lon },
      zoom: 15,
      mapTypeId: 'roadmap',
      disableDefaultUI: true
    });
    new google.maps.Marker({ position: { lat, lng: lon }, map: map });
    mapDiv._google_map = map;
    console.log('Google Map inicializado OK');
  } else {
    console.log('Falta algún elemento para inicializar el mapa');
  }
};

// SPA: asegura que init se llama cuando el fragmento se carga
document.addEventListener('fragment:loaded', (ev) => {
  const { container, path } = ev.detail || {};
  if (container) init({ container, path });
});