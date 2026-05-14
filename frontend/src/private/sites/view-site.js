import { navigateTo } from '../../navigation-handler.js';
import { fetchWithAuth } from '../../auth.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/add-advanced-marker.js';

const qs  = (s) => document.querySelector(s);

const cancelViewSite = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/sites/table-view', true), 1000);
}

export const startViewMap = async () => {
  const apiKey = googleMapsConfig.apiKey;

  const id = qs('#siteId').value;

  try {
    console.log('Loading Google Maps API...');
    await loadGoogleMapsAPI(apiKey);
    const map = await initMap('map', {
      mapTypeId: 'hybrid',
      zoom: 10,
    });

    const response = await fetchWithAuth(`/api/sites/${id}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    const siteData = await response.json();

    console.log('Site data:', siteData);
    const initialMarker = await addAdvancedMarker(map, siteData.name, siteData.lat, siteData.lon);

    const bounds = new google.maps.LatLngBounds();
    bounds.extend({ lat: parseFloat(siteData.lat), lng: parseFloat(siteData.lon) });
    map.fitBounds(bounds);
    map.setZoom(15);

    return { map, siteData, initialMarker };

  } catch (error) {
    console.error('[site-map.js] Error al cargar la API de Google Maps:'
                                                                    , error);
  }
}

function bindViewSite() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewSite);
    }
}

(async function init() {
  bindViewSite();
  console.log('Initializing view site page...');

  await startViewMap();
  console.log('View site page initialized.');
})();