import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { enableMarkerDrag } from '../../shared/maps/enable-marker-drag.js';

const qs  = (s) => document.querySelector(s);
const qa  = (s) => document.querySelectorAll(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-cancel');


async function startCreateMap() {
  const apiKey = googleMapsConfig.apiKey;

  console.log('Loading Google Maps API...');
  await loadGoogleMapsAPI(apiKey);
  const map = await initMap('map', {
    mapTypeId: 'hybrid',
    zoom: 10,
  });

  map.addListener('click', async (event) => {
    const lat = event.latLng.lat();
    const lon = event.latLng.lng();

    if(!mainMarker){
      mainMarker = await addAdvancedMarker(
                                map, 'Nombre del sitio', lat, lon);

      enableMarkerDrag(mainMarker, (newPos) => {
          qs('#siteLat').value = newPos.lat();
          qs('#siteLon').value = newPos.lng();
      });


    } else {
      mainMarker.position = { lat, lng: lon };
    }
    qs('#siteLat').value = lat;
    qs('#siteLon').value = lon;
  });
}


async function onSubmitCreate(e) {
  e.preventDefault();

  const err = qs('#createSiteError');
  const ok  = qs('#createSiteOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  const projectId = Number(qs('#siteProjectId')?.value);
  const name     = qs('#siteName')?.value?.trim();
  const code     = qs('#siteCode')?.value?.trim() || null;
  const address  = qs('#siteAddress')?.value?.trim() || null;
  const latStr   = qs('#siteLat')?.value?.trim();
  const lonStr   = qs('#siteLon')?.value?.trim();
  const timeZone = qs('#siteTz')?.value?.trim() || null;
  const active   = !!qs('#siteActive')?.checked;

  // Parseo seguro de coordenadas (null si vacío)
  const lat = latStr ? Number(latStr) : null;
  const lon = lonStr ? Number(lonStr) : null;

  // Validaciones mínimas
  if (!projectId) { err && (err.textContent = 'Debes seleccionar un proyecto.'); return; }
  if (!name)      { err && (err.textContent = 'El nombre es obligatorio.'); return; }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createSiteForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/sites/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        projectId,
        name,
        address,
        lat,
        lon,
        timeZone,
        active
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    ok && (ok.style.display = 'block');

    setTimeout(() => {
      closeModal();
      navigateTo('/private/sites/table-view'); // recarga el listado
    }, 600);

  } catch (e2) {
    err && (err.textContent = e2.message || 'No se pudo crear el sitio');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}


const cancelCreateSite = () => {
    displayAlert(alertCancel, 'La creación del sitio a sido cancelada.', 2000);
    setTimeout(() => navigateTo('/private/sites/table-view', true), 2000);
}


function bindCreateSite() {
    const createBtn = qs('.btn-primary');
    if (createBtn) {
        createBtn.addEventListener('click', onSubmitCreate);
    }
    const cancelBtn = qs('.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelCreateSite);
    }
  }


(function init() {
  bindCreateSite();

  startCreateMap();

})();