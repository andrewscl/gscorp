import { fetchWithAuth } from '../../auth.js';

// Encuentra el sitio más cercano y su distancia
function getNearestSite(userLat, userLon, sites) {
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

let attendanceSites = [];

async function loadSites() {
  try {
    const res = await fetchWithAuth('/api/attendance/sites', { credentials: 'same-origin' });
    if (!res.ok) throw new Error(`Error cargando sitios: ${res.status}`);
    attendanceSites = await res.json();
  } catch (e) {
    console.error("No se pudo cargar la lista de sitios:", e);
    attendanceSites = [];
  }
}

function initAttendanceWidget() {
  const root = document.getElementById('att-widget');
  if (!root || root.dataset.attInit === '1') return;
  root.dataset.attInit = '1';

  const statusEl = document.getElementById('att-status');
  const btnIn    = document.getElementById('att-in');
  const btnOut   = document.getElementById('att-out');
  const endpoint = root.dataset.punchEndpoint || '/api/attendance/punch';

  const setStatus = (t, isError = false) => {
    if (!statusEl) return;
    statusEl.textContent = t;
    statusEl.style.color = isError ? '#b00020' : '#444';
  };

  const disable = (v) => {
    if (btnIn)  btnIn.disabled  = v;
    if (btnOut) btnOut.disabled = v;
  };

  const isSecureCtx = () =>
    location.protocol === 'https:' ||
    location.hostname === 'localhost' ||
    location.hostname === '127.0.0.1';

  const getPosition = () => new Promise((resolve, reject) => {
    if (!navigator.geolocation) return reject(new Error('Geolocalización no soportada'));
    navigator.geolocation.getCurrentPosition(
      (p) => resolve(p),
      (e) => reject(new Error(e.message || 'No se pudo obtener ubicación')),
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
    );
  });

  async function punch(kind) {
    const action = (String(kind).toUpperCase() === 'OUT') ? 'OUT' : 'IN';

    try {
      disable(true);

      if (!isSecureCtx()) {
        setStatus('Activa HTTPS (o usa localhost) para solicitar ubicación en móviles.', true);
        return;
      }

      setStatus('Obteniendo ubicación...');
      const pos = await getPosition();

      // Espera a que los sitios estén cargados
      if (!attendanceSites.length) {
        setStatus('Cargando sitios, por favor espera...', true);
        await loadSites();
        if (!attendanceSites.length) {
          setStatus('No se pudo cargar la lista de sitios.', true);
          disable(false);
          return;
        }
      }

      // Determina el sitio más cercano
      const nearestSite = getNearestSite(pos.coords.latitude, pos.coords.longitude, attendanceSites);

      if (!nearestSite || nearestSite.distance > 35) {
        setStatus(`No puede marcar asistencia: está a ${nearestSite ? nearestSite.distance.toFixed(1) : 'N/A'} metros del sitio más cercano (máx 35m).`, true);
        disable(false);
        showSiteInfo(nearestSite);
        return;
      } else {
        // Mostrar mensaje de sitio
        showSiteInfo(nearestSite);
      }

      setStatus(`Marcando asistencia en el sitio: ${nearestSite.name}`);

      const payload = {
        action, // "IN" | "OUT"
        lat: pos.coords.latitude,
        lon: pos.coords.longitude,
        accuracy: pos.coords.accuracy,
        siteId: nearestSite.id
      };

      setStatus('Registrando asistencia...');

      // Timeout de red (20s)
      const ac = new AbortController();
      const t  = setTimeout(() => ac.abort('timeout'), 20000);

      const res = await fetchWithAuth(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload),
        signal: ac.signal,
        credentials: 'same-origin'
      }).finally(() => clearTimeout(t));

      if (!res.ok) {
        let reason = `Error ${res.status}`;
        try {
          const errJson = await res.json();
          if (errJson?.error) reason = errJson.error + (errJson.details ? `: ${errJson.details}` : '');
        } catch {
          try { reason = (await res.text()) || reason; } catch {}
        }
        throw new Error(reason);
      }

      const out = await res.json().catch(() => ({}));
      
      const hora = out.ts ? new Date(out.ts).toLocaleTimeString('es-CL', { hour12: false }) : '';
      const distancia = typeof out.distanceMeters === 'number' ? `${out.distanceMeters} metros` : '';
      setStatus(`Marcación registrada correctamente${hora ? ' a las ' + hora : ''}${distancia ? ' a ' + distancia : ''}. ✅`);

      updateAttendanceButtons();

    } catch (e) {
      setStatus('Error en marcación: ' + (e?.message || 'desconocido'), true);
    } finally {
      disable(false);
    }
  }

  btnIn?.addEventListener('click', () => punch('IN'));
  btnOut?.addEventListener('click', () => punch('OUT'));

  updateAttendanceButtons();
  showCurrentLocationOnMap();
}

// Auto-init al cargar y cuando tu router inserte el fragmento
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', async () => {
    await loadSites();
    initAttendanceWidget();
  });
} else {
  loadSites().then(initAttendanceWidget);
}
document.addEventListener('content:loaded', async () => {
  await loadSites();
  initAttendanceWidget();
});

async function updateAttendanceButtons() {
  const btnIn  = document.getElementById('att-in');
  const btnOut = document.getElementById('att-out');

  // Consulta tu API (ajusta la URL si es necesario)
  try {
    const res = await fetchWithAuth('/api/attendance/last-punch', { credentials: 'same-origin' });

    if (!res.ok) {
      // Si falla la consulta, muestra solo entrada por defecto
      btnIn.style.display = '';
      btnOut.style.display = 'none';
      return;
    }

    const lastPunch = await res.json();
    const action = lastPunch.action ?
    lastPunch.action.toUpperCase() : null;

    if (!action || action === 'OUT') {
      btnIn.style.display = '';
      btnOut.style.display = 'none';
    } else if (action === 'IN') {
      btnIn.style.display = 'none';
      btnOut.style.display = '';
    }
  } catch {
    btnIn.style.display = '';
    btnOut.style.display = 'none';
  }
}

function showCurrentLocationOnMap() {
  const mapDiv = document.getElementById('att-map');
  if (!mapDiv) return;

  // Limpia el div si ya tenía un mapa
  if (mapDiv._google_map) {
    mapDiv._google_map = null;
    mapDiv.innerHTML = "";
  }

  if (!navigator.geolocation) {
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>Geolocalización no soportada</div>";
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;
      if (window.google && window.google.maps) {
        const map = new google.maps.Map(mapDiv, {
          center: { lat, lng: lon },
          zoom: 17,
          mapTypeId: 'roadmap',
          disableDefaultUI: true
        });
        new google.maps.Marker({ position: { lat, lng: lon }, map: map });
        mapDiv._google_map = map;
      } else {
        mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>Google Maps no cargado</div>";
      }
    },
    (err) => {
      mapDiv.innerHTML = `<div style='padding:1em;text-align:center;color:#b00020'>No se pudo obtener ubicación.<br>${err.message}</div>`;
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

function getDistanceMeters(lat1, lon1, lat2, lon2) {
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

function showSiteInfo(nearestSite) {
  const infoEl = document.getElementById('att-site-info');
  if (!infoEl) return;

  if (!nearestSite) {
    infoEl.textContent = "";
    return;
  }

  if (nearestSite.distance <= 35) {
    infoEl.textContent = `Estás en el sitio "${nearestSite.name}". Puedes marcar asistencia aquí.`;
    infoEl.style.color = "#059669"; // verde
  } else {
    infoEl.textContent = `El sitio más cercano es "${nearestSite.name}" a ${nearestSite.distance.toFixed(1)} metros. Acércate para marcar.`;
    infoEl.style.color = "#d97706"; // amarillo
  }
}