import { fetchWithAuth } from '../../auth.js';

// Función para cargar el script de Google Maps - Moderno y modular
const loadGoogleMapsAPI = (() => {
  let isScriptLoaded = false; // Bandera para evitar duplicados
  let scriptPromise = null; // Promesa para gestionar la carga del script

  return (apiKey) => {
    if (isScriptLoaded) {
      return scriptPromise; // Si el script ya está cargado, devuelve la promesa
    }

    // Crear la promesa de carga del script
    scriptPromise = new Promise((resolve, reject) => {
      // Crear el script dinámicamente
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&v=weekly`;
      script.async = true;
      script.defer = true;

      // Resolver cuando el script se cargue correctamente
      script.onload = () => {
        console.log('[Google Maps API] Script cargado correctamente.');
        isScriptLoaded = true;
        resolve();
      };

      // Rechazar si ocurre algún error al cargar el script
      script.onerror = (error) => {
        console.error('[Google Maps API] Error al cargar el script:', error);
        reject(new Error('Error al cargar Google Maps API'));
      };

      // Adjuntar el script al <head>
      document.head.appendChild(script);
    });

    return scriptPromise; // Devolver la promesa para su uso
  };
})();


// Función para inicializar el mapa
const initMap = async () => {
  const mapContainer = document.getElementById('site-map'); // Div contenedor del mapa
  if (!mapContainer) {
    console.warn('[initMap] Contenedor #site-map no encontrado en el DOM.');
    return;
  }

  try {
    // Importar las bibliotecas necesarias usando el enfoque moderno de Google
    const { Map } = await google.maps.importLibrary("maps");

    // Crear e inicializar el mapa
    const map = new Map(mapContainer, {
      center: { lat: -33.4489, lng: -70.6693 },
      zoom: 12, // Nivel de zoom inicial
      mapId: googleMapsConfig.mapId, // Personaliza con Map ID de Google Cloud
      disableDefaultUI: true, // Desactiva los controles predeterminados
    });
    console.log('[initMap] Mapa inicializado.');

    window.mapInstance = map; // Guardar la instancia del mapa globalmente

    // Obtener y añadir los sitios al mapa
    await fetchSites();

  } catch (error) {
    console.error('[initMap] Error al inicializar el mapa:', error);
  }
};


// Obtiene los sitios desde el servidor usando fetchWithAuth
async function fetchSites() {
  try {
    const res = await fetchWithAuth('/api/sites/projections-by-user', {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!res.ok) {
      console.log('Error al cargar sitios. Intente nuevamente.');
      return;
    }

    const siteData = await res.json();

    // Valida que es un arreglo y que los elementos tienen las propiedades necesarias
    if (!Array.isArray(siteData) || !siteData.every(site => site.id && site.lat && site.lon)) {
    console.log('Datos de sitios inválidos.');
    return;
    }

    addSitesToMapAndSelect(siteData); // Llenar el mapa y las opciones del select

  } catch (error) {
    console.error('Fallo al obtener sitios:', error);
    console.log('Error al cargar sitios. Intente nuevamente.');
  }
}


// Función para agregar sitios al mapa como marcadores
async function addSitesToMapAndSelect(sites) {

    const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");

  sites.forEach(site => {

    // crear una instancia para LatLngBounds
    const bounds = new google.maps.LatLngBounds();

    // Crear contenido personalizado para el tooltip
    const markerContent = document.createElement('div');
    markerContent.style.backgroundColor = '#fff';
    markerContent.style.border = '1px solid grey';
    markerContent.style.padding = '4px 8px';
    markerContent.style.borderRadius = '8px';
    markerContent.style.boxShadow = '0 2px 6px rgba(0,0,0,0.3)';
    markerContent.style.position = 'absolute';
    markerContent.style.top = '-40px'; // Encima del pin
    markerContent.style.left = '50%';
    markerContent.style.transform = 'translateX(-50%)';
    markerContent.style.whiteSpace = 'nowrap';
    markerContent.style.display = 'none'; // Ocultar inicialmente
    markerContent.textContent = `Sitio: ${site.name || 'Sin Nombre'}`; // Texto dinámico

    // Crear estilo del marcador (PinElement)
    const pin = new PinElement({
      scale: 0.8,
      glyphColor: '#3176e3',
      background: '#359dd1',
      borderColor: '#1d4d9b',
    });

    // Crear contenedor para el marcador y el contenido dinámico
    const markerContainer = document.createElement('div');
    markerContainer.style.position = 'relative';
    markerContainer.appendChild(pin.element); // Añadir el PinElement
    markerContainer.appendChild(markerContent); // Añadir el tooltip

    // Crear el marcador avanzado
    const advancedMarker = new AdvancedMarkerElement({
      map: window.mapInstance, // Asociar al mapa inicializado
      position: { lat: site.lat, lng: site.lon }, // Posición del sitio
      content: markerContainer, // Contenedor para el pin y el contenido
    });

    // Hover para mostrar el tooltip
    markerContainer.addEventListener('mouseenter', () => {
      markerContent.style.display = 'block'; // Mostrar el tooltip
    });

    markerContainer.addEventListener('mouseleave', () => {
      markerContent.style.display = 'none'; // Ocultar el tooltip
    });

    // Agregar las coordenadas del sitio a bounds
    bounds.extend(new google.maps.LatLng(site.lat, site.lon));

    console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${site.id}`);
  });

  // Ajustar el mapa para mostrar todos los marcadores
  window.mapInstance.fitBounds(bounds);

}


/* --- init --- */
(async function init() {

  console.log('[init] IIFE iniciado');

  const apiKey = googleMapsConfig.apiKey;

  try {
    //Cargar y esperar Google Maps API
    await loadGoogleMapsAPI(apiKey);

    // Inicializar el mapa
    initMap();

  } catch (error) {
    console.error('[site-map.js] Error al cargar la API de Google Maps:', error);
  }

})();