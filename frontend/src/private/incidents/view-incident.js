import { navigateTo } from '../../navigation-handler.js';

/**
 * view-incident.js
 *
 * Comportamiento para la vista "Ver Incidente":
 * - Maneja el botón "Volver al listado" usando navigateTo(...) (SPA) o location.href como fallback.
 * - Inicializa un lightbox simple para ampliar la fotografía al hacer click en .incident-photo-thumb.
 * - Si existen inputs con coordenadas (#incidentLat, #incidentLon) y un contenedor #incidentMap,
 *   muestra un mapa de solo lectura usando Google Maps (si está cargado).
 * - Escucha los eventos DOMContentLoaded y custom event content:loaded para inicializar.
 */

/* ------------------ Back button (SPA-aware) ------------------ */
function initBackButton() {
  const backBtn = document.querySelector('.actions .vs-btn.vs-secondary[data-path]') ||
                  document.querySelector('[data-path="/private/incidents/table-view"]');
  if (!backBtn) return;

  backBtn.addEventListener('click', (e) => {
    e.preventDefault();
    const path = backBtn.getAttribute('data-path') || '/private/incidents/table-view';
    if (typeof navigateTo === 'function') {
      try {
        navigateTo(path);
      } catch (err) {
        // fallback simple
        console.warn('[view-incident] navigateTo falló, usando location.href', err);
        window.location.href = path;
      }
    } else {
      window.location.href = path;
    }
  });
}

/* ------------------ Lightbox simple ------------------ */
function createLightbox() {
  const overlay = document.createElement('div');
  overlay.className = 'vi-overlay';
  overlay.style.cssText = 'position:fixed;inset:0;display:flex;align-items:center;justify-content:center;background:rgba(0,0,0,0.75);z-index:10000;padding:20px;';
  overlay.setAttribute('role', 'dialog');
  overlay.setAttribute('aria-hidden', 'true');

  const wrapper = document.createElement('div');
  wrapper.style.cssText = 'max-width:calc(100% - 40px);max-height:calc(100% - 40px);position:relative;display:flex;align-items:center;justify-content:center;';

  const img = document.createElement('img');
  img.style.cssText = 'max-width:100%;max-height:100%;border-radius:8px;box-shadow:0 6px 30px rgba(0,0,0,0.45);background:#fff;';

  const closeBtn = document.createElement('button');
  closeBtn.type = 'button';
  closeBtn.textContent = '×';
  closeBtn.setAttribute('aria-label', 'Cerrar');
  closeBtn.style.cssText = 'position:absolute;top:8px;right:8px;width:36px;height:36px;border-radius:6px;background:rgba(255,255,255,0.95);border:none;font-size:22px;cursor:pointer;';

  wrapper.appendChild(img);
  wrapper.appendChild(closeBtn);
  overlay.appendChild(wrapper);

  function close() {
    if (overlay.parentNode) overlay.parentNode.removeChild(overlay);
    document.removeEventListener('keydown', onKey);
  }

  function onKey(e) {
    if (e.key === 'Escape') close();
  }

  overlay.addEventListener('click', (ev) => {
    if (ev.target === overlay) close();
  });

  closeBtn.addEventListener('click', close);

  return {
    open(src, alt) {
      img.src = src;
      img.alt = alt || '';
      document.body.appendChild(overlay);
      document.addEventListener('keydown', onKey);
      closeBtn.focus();
    },
    close
  };
}

function initLightbox() {
  const thumbs = Array.from(document.querySelectorAll('.incident-photo-thumb'));
  if (!thumbs.length) return;
  const lb = createLightbox();

  thumbs.forEach((thumb) => {
    // find parent link href (if exists) as preferred source
    const parentLink = thumb.closest('a');
    thumb.style.cursor = 'zoom-in';

    thumb.addEventListener('click', (ev) => {
      ev.preventDefault();
      const src = (parentLink && parentLink.getAttribute('href')) || thumb.getAttribute('src') || thumb.getAttribute('data-src');
      if (!src) {
        console.warn('[view-incident] imagen sin src disponible');
        return;
      }
      try {
        lb.open(src, thumb.alt || 'Foto incidente');
      } catch (err) {
        console.error('[view-incident] error abriendo lightbox', err);
      }
    });

    thumb.addEventListener('error', () => {
      thumb.style.opacity = '0.6';
      thumb.style.filter = 'grayscale(40%)';
    });
  });
}

/* ------------------ Mapa de solo visualización (opcional) ------------------ */
function showIncidentLocationOnMap() {
  const mapDiv = document.getElementById('incidentMap');
  const latInput = document.getElementById('incidentLat');
  const lonInput = document.getElementById('incidentLon');

  if (!mapDiv || !latInput || !lonInput) return;

  const lat = parseFloat(latInput.value);
  const lon = parseFloat(lonInput.value);

  if (!lat || !lon || Number.isNaN(lat) || Number.isNaN(lon)) {
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#888'>No hay coordenadas para mostrar el mapa.</div>";
    return;
  }

  if (window.google && window.google.maps) {
    const mapInstance = new google.maps.Map(mapDiv, {
      center: { lat, lng: lon },
      zoom: 16,
      mapTypeId: 'roadmap',
      disableDefaultUI: true
    });
    new google.maps.Marker({ position: { lat, lng: lon }, map: mapInstance });
    // store ref if needed later
    mapDiv._google_map = mapInstance;
  } else {
    mapDiv.innerHTML = "<div style='padding:1em;text-align:center;color:#b00020'>Google Maps no cargado</div>";
  }
}

/* ------------------ Init / event listeners ------------------ */
function init() {
  try {
    initBackButton();
    initLightbox();
    showIncidentLocationOnMap();
  } catch (err) {
    console.error('[view-incident] init error', err);
  }
}

// auto-init on DOM ready and on partial content load
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
document.addEventListener('content:loaded', init);

export { init as initViewIncident };