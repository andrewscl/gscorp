// Este script inicializa el mapa de la visita de supervisión en modo solo lectura.
// Espera que el div #visit-map tenga los atributos data-lat y data-lon con las coordenadas.

document.addEventListener("DOMContentLoaded", () => {
  const mapDiv = document.getElementById('visit-map');
  if (!mapDiv) return;

  const lat = parseFloat(mapDiv.dataset.lat);
  const lon = parseFloat(mapDiv.dataset.lon);

  if (isNaN(lat) || isNaN(lon)) return;

  // Espera a que Google Maps esté cargado
  function initMap() {
    if (window.google && window.google.maps) {
      const map = new google.maps.Map(mapDiv, {
        center: { lat, lng: lon },
        zoom: 17,
        disableDefaultUI: true,
        mapTypeId: "roadmap"
      });
      new google.maps.Marker({
        position: { lat, lng: lon },
        map,
        title: "Ubicación de la visita"
      });
    } else {
      setTimeout(initMap, 250);
    }
  }

  initMap();
});