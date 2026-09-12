import { fetchWithAuth } from "../../auth.js";
import loadGoogleMapsAPI from "./../../shared/maps/googlemaps-loader.js"
import { initMap } from "../../shared/maps/init-map.js";
import { addAdvancedMarker } from "../../shared/maps/advanced-marker.js";

const qs  = (s) => document.querySelector(s);

export async function startSiteMap() {
    const siteExternalId = qs('#siteExternalId')?.value || '';
    const mapElement = qs('#map');
    if (!siteExternalId || !mapElement) return null;
    try {
        await loadGoogleMapsAPI(googleMapsConfig.apiKey);
        const map = await initMap('map', {
        mapTypeId: 'hybrid',
        zoom: 10,
        });
        const response = await fetchWithAuth(`/api/sites/${siteExternalId}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        });
        if (!response.ok) throw new Error(`Error ${response.status} al obtener sitio`);
        const siteData = await response.json();
        const DEFAULT_CENTER = { lat: -33.4489, lng: -70.6693 };
        const rawLat = parseFloat(siteData.lat);
        const rawLon = parseFloat(siteData.lon);
        const hasValidCoords = !isNaN(rawLat) && !isNaN(rawLon);
        const lat = hasValidCoords ? rawLat : DEFAULT_CENTER.lat;
        const lon = hasValidCoords ? rawLon : DEFAULT_CENTER.lng;
        const initialMarker = await addAdvancedMarker(map, siteData.name, lat, lon);
        if (hasValidCoords) {
            const bounds = new google.maps.LatLngBounds();
            bounds.extend({lat: lat, lng: lon});
            map.fitBounds(bounds); 
            map.setZoom(15);
        }
        return { map, siteData, initialMarker, hasValidCoords };
    } catch (error) {
        console.error('[site-map.js] Error al cargar la API de Google Maps:', error);
        return null;
    }
}