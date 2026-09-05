import { navigateTo } from '../../navigation-handler.js';
import { fetchWithAuth } from '../../auth.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js';

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

const loadSiteZones = async () => {
  const siteExternalId = qs('#siteExternalId').value;
  try{
    const response = await fetchWithAuth(`/api/site-zones/list?siteExternalId=${siteExternalId}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });
    if (!response.ok) {
      throw new Error(`Error al obtener las zonas del sitio: ${response.status}`);
    }
    const siteZones = await response.json();
    const siteZoneList = qs('#site-zone-tbody');
    if (!siteZoneList) {
      console.error('[view-site.js] Elemento no encontrado para listar zonas del sitio.');
      return;
    }
    siteZoneList.innerHTML = '';
    if (siteZones.length === 0) {
      siteZoneList.innerHTML = `<tr>
                                  <td colspan="4" class="text-center text-muted">Sin zonas del sitio</td>
                                </tr>`;
      return;
    }
    siteZones.forEach(zone => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${zone.name}</td>
        <td>${zone.status}</td>
      `;
      siteZoneList.appendChild(row);
    });
  } catch (error) {
    console.error('[view-site.js] Error al cargar las zonas del sitio:', error);
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
  await loadSiteZones();
})();