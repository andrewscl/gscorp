import { navigateTo } from '../../navigation-handler.js';

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

  // Mapa Google Maps solo visualización
  const latInput = container.querySelector('#siteLat');
  const lonInput = container.querySelector('#siteLon');
  const mapDiv = container.querySelector('#siteMap');
  if (mapDiv && latInput && lonInput && window.google && window.google.maps) {
    // Evita inicializar dos veces si el usuario navega rápido
    if (mapDiv._google_map) return;

    const lat = parseFloat(latInput.value) || -33.45;
    const lon = parseFloat(lonInput.value) || -70.66;
    console.log('Usando lat:', lat, 'lon:', lon);
    const map = new google.maps.Map(mapDiv, {
      center: { lat, lng: lon },
      zoom: 15, // Ajusta el zoom aquí
      mapTypeId: 'roadmap',
      disableDefaultUI: true // oculta controles extra
    });
    new google.maps.Marker({ position: { lat, lng: lon }, map: map });
    mapDiv._google_map = map; // Marca como inicializado
    console.log('Google Map inicializado OK');
  } else {
    console.log('Falta algún elemento o Google Maps no cargado', { mapDiv, latInput, lonInput, google: window.google });
  }

  // Si en el futuro agregas funciones (copiar datos, imprimir, exportar), puedes hacerlo aquí.
}

// SPA: asegura que init se llama cuando el fragmento se carga
document.addEventListener('fragment:loaded', (ev) => {
  const { container, path } = ev.detail || {};
  if (container) init({ container, path });
});