//Funciones de utilidad operativa
export function getDistanceMeters(lat1, lon1, lat2, lon2) {
  // Fórmula Haversine
  const R = 6371000; // Radio tierra en metros
  const toRad = x => x * Math.PI / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

export function getNearestSite(userLat, userLon, sites) {
  let nearest = null;
  let minDistance = Infinity;
  for (const site of sites) {
    if (typeof site.lat !== 'number' || typeof site.lon !== 'number') continue;
    const d = getDistanceMeters(userLat, userLon, site.lat, site.lon);
    if (d < minDistance) {
      minDistance = d;
      nearest = { ...site, distance: d };
    }
  }
  return nearest;
}

export const getCurrentPosition = () => new Promise((resolve, reject) => {
  if (!navigator.geolocation) return reject(new Error('Geolocalización no soportada'));
  navigator.geolocation.getCurrentPosition(
    (p) => resolve(p),
    (e) => reject(new Error(e.message || 'No se pudo obtener ubicación')),
    { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
  );
});