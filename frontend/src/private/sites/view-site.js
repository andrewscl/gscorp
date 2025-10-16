import { navigateTo } from '../../navigation-handler.js';

// Protege el registro del event listener
if (!window._viewSiteInitRegistered) {
  document.addEventListener('fragment:loaded', (ev) => {
    const { container, path } = ev.detail || {};
    if (container) init({ container, path });
  });
  window._viewSiteInitRegistered = true;
}

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

  // Limpia el marcador de inicialización anterior
  if (mapDiv && mapDiv._google_map) {
    mapDiv._google_map = null;
    if (mapDiv.firstChild) mapDiv.innerHTML = "";
  }

  window._pendingMapInit = { latInput, lonInput, mapDiv };

  // Si Google Maps ya está cargado, inicializa el mapa
  if (window.google && window.google.maps) {
    window.onGoogleMapsReady();
  }
}

window.onGoogleMapsReady = function() {
  const pending = window._pendingMapInit;
  if (!pending) return;

  const { latInput, lonInput, mapDiv } = pending;
  if (mapDiv && latInput && lonInput) {
    // Evita doble inicialización en el mismo fragmento
    if (mapDiv._google_map) return;

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
    // No lances error si el fragmento no tiene mapa
    console.log('Fragmento sin mapa o sin coordenadas, no se inicializa Google Maps');
  }
};