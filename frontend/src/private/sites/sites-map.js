import { fetchWithAuth } from '../../auth.js';

let map, markers = [], sites = [];

let mapInitialized = false;

// Cargar dinámicamente el script de Google Maps
function loadGoogleMapsAPI(apiKey, mapId) {
  return new Promise((resolve, reject) => {
    // Verificar si ya se cargó el script
    if (window.google && google.maps) {
      console.log('[loadGoogleMapsAPI] Google Maps ya estaba disponible.');
      resolve(); // Google Maps ya está listo
      return;
    }

    // Evitar múltiple carga del script
    if (document.getElementById('google-maps-script')) {
      console.log('[loadGoogleMapsAPI] Script ya existe en el DOM.');
      const interval = setInterval(() => {
        if (window.google && google.maps) {
          clearInterval(interval);
          resolve();
        }
      }, 200);
      return;
    }

    // Crear el script
    const script = document.createElement('script');
    script.id = 'google-maps-script'; //Agregar un id unico para rastreo
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=marker&map_ids=${mapId}&callback=googleMapsLoaded&loading=async`;
    script.async = true;
    script.defer = true;
    script.setAttribute('loading', 'async');

    // El callback global será ejecutado cuando el script termine de cargarse
    window.googleMapsLoaded = () => {
      console.log('[googleMapsLoaded] Google Maps cargado exitosamente.');
      resolve();
    };

    // Rechaza la promesa si ocurre un error al cargar el script
    script.onerror = () => {
      console.error('[loadGoogleMapsAPI] Error cargando Google Maps API.');
      reject(new Error('No se pudo cargar Google Maps API'));
    };

    // Agregar el script al DOM
    document.head.appendChild(script);
  });
}

// Inicializa el mapa
function initMap() {

  if(mapInitialized) {
    console.log('[initMap] Mapa ya inicializado, no se hace nada.');
    return;
  }

  const mapDiv = document.getElementById('site-map');
  if (!mapDiv) {
    console.error('[initMap] Contenedor del mapa no encontrado.');
    return;
  }

  // Crea el mapa con un centro y zoom predeterminados
  map = new google.maps.Map(mapDiv, {
    center: { lat: -33.45, lng: -70.65 }, // Por defecto, Santiago
    zoom: 8,
    mapTypeId: 'roadmap',
    mapId: googleMapsConfig.mapId
  });

  // Escucha el select para centrarse en sitios seleccionados
  const select = document.getElementById('site-select');
  select?.addEventListener('change', onSiteSelected);

  // Obtén los sitios desde tu REST API
  fetchSites();

  console.log('[initMap] Mapa inicializado exitosamente.');
  mapInitialized = true; // Marca como inicializado

}


// Muestra un error en pantalla
function showMapError(message) {
  const errorDiv = document.getElementById('site-map-error');
  if (errorDiv) {
    errorDiv.textContent = message;
    errorDiv.style.display = "block";
  }
}

// Función para limpiar el mensaje de error cuando ya no aplica
function clearMapError() {
  const errorDiv = document.getElementById('site-map-error');
  if (errorDiv) errorDiv.style.display = "none";
}

// Agrega los sitios al mapa y llena el select
function addSitesToMapAndSelect(siteData) {
  markers.forEach(marker => marker.setMap(null)); // Limpia marcadores anteriores
  markers = [];
  sites = siteData; // Actualiza la lista global de sitios

  const select = document.getElementById('site-select');
  if (select) {
    select.innerHTML = '<option value="">Seleccionar sitio</option>'; // Resetea el select
  }

  const bounds = new google.maps.LatLngBounds();
  siteData.forEach(site => {
    if (typeof site.lat === 'number' && typeof site.lon === 'number') {
      const position = { lat: site.lat, lng: site.lon };

      // Create content for the marker
      const markerContent = document.createElement('div');
      markerContent.innerHTML = `
        <div style="background-color: white; border: 1px solid black; padding: 5px; border-radius: 3px;">
          <strong>${site.name}</strong>
        </div>
      `;

      // Crear un marcador avanzado
      const marker = new google.maps.marker.AdvancedMarkerElement({
        map,                     // Mapa al que se vincula el marcador
        position: position,      // Posición del marcador
        title: site.name,        // Título (texto de acceso rápido)
        content: markerContent,  // Contenido HTML personalizado
      });

      // InfoWindow para mostrar información del sitio al hacer clic
      const infoWindow = new google.maps.InfoWindow({
        content: `<h4>${site.name}</h4><p>Dirección: ${site.address}</p><p>Zona horaria: ${site.timeZone}</p>`,
      });

      marker.addListener('click', () => infoWindow.open(map, marker));
      markers.push(marker);

      // Extender límites para encerrar todos los marcadores en el mapa
      bounds.extend(position);

      // Agregar una entrada a la etiqueta <select>
      if (select) {
        const option = document.createElement('option');
        option.value = site.id;
        option.textContent = site.name;
        select.appendChild(option);
      }
    }
  });

  // Ajusta el mapa para mostrar todos los marcadores
  if (siteData.length > 0) {
    map.fitBounds(bounds);
  } else {
    // Centro predeterminado cuando no hay sitios válidos
    map.setCenter({ lat: -33.45, lng: -70.65 });
    map.setZoom(8);
    showMapError('No hay sitios para mostrar.');
  }

  onSiteHover();
}

// Maneja el cambio en el select
function onSiteSelected() {
  const select = document.getElementById('site-select');
  if (!select) return;

  const siteId = select.value;
  const foundSite = sites.find(site => String(site.id) === siteId); // Busca el objeto del sitio seleccionado
  if (foundSite) {
    map.setCenter({ lat: foundSite.lat, lng: foundSite.lon });
    map.setZoom(15);
  }
}

// Obtiene los sitios desde el servidor usando fetchWithAuth
async function fetchSites() {
  try {
    const res = await fetchWithAuth('/api/sites/projections-by-user', {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!res.ok) {
      showMapError('Error al cargar sitios. Intente nuevamente.');
      return;
    }

    const siteData = await res.json();
    clearMapError();
    // Valida que es un arreglo y que los elementos tienen las propiedades necesarias
    if (!Array.isArray(siteData) || !siteData.every(site => site.id && site.lat && site.lon)) {
    showMapError('Datos de sitios inválidos.');
    return;
    }

    addSitesToMapAndSelect(siteData); // Llenar el mapa y las opciones del select
  } catch (error) {
    console.error('Fallo al obtener sitios:', error);
    showMapError('Error al cargar sitios. Intente nuevamente.');
  }
}

function onSiteHover() {
  const select = document.getElementById('site-select');
  if (!select) return;

  select.addEventListener('mouseover', (event) => {
    const hoveredSiteId = event.target.value;
    if (!hoveredSiteId) return;

    const hoveredSite = sites.find(site => String(site.id) === hoveredSiteId);

    if (hoveredSite) {
      // Cambia el contenido de los marcadores al hacer hover
      markers.forEach(marker => {
        if (marker.title === hoveredSite.name) {
          marker.content.innerHTML = `
            <div style="background-color: #0000FF; color: white; border-radius: 8px; padding: 5px;">
              <strong>${hoveredSite.name}</strong>
            </div>
          `;
        } else {
          marker.content.innerHTML = `
            <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
              <strong>${marker.title}</strong>
            </div>
          `;
        }
      });
    }
  });

  select.addEventListener('mouseleave', () => {
    // Restaura los marcadores al salir del hover
    markers.forEach(marker => {
      marker.content.innerHTML = `
        <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
          <strong>${marker.title}</strong>
        </div>
      `;
    });
  });
}

/* --- init --- */
(function init() {

  if (window.google && google.maps) {
    console.log('[init] Google Maps ya estaba cargada anteriormente.');
    initMap(); // Inicializar directamente si ya está listo
    return;
  }

  console.log('Google Maps Config:', googleMapsConfig);
  console.log('[init] IIFE iniciado');

  // Cargar Google Maps API y luego inicializar el mapa
  loadGoogleMapsAPI(googleMapsConfig.apiKey, googleMapsConfig.mapId)
    .then(() => {
      console.log('[init] Google Maps API lista. Inicializando mapa.');
      initMap();
    })
    .catch((error) => {
      console.error('[init] Error durante la carga de Google Maps:', error);
      showMapError('No se pudo cargar Google Maps. Intente más tarde.');
  });



})();