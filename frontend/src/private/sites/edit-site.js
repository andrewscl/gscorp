import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';
import { enableMarkerDrag } from '../../shared/maps/enable-marker-drag.js';
import { loadAndRenderSiteZones } from './../operations/site-zones/site-zones-service.js';
import { startSiteMap } from './start-sites-map.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

async function deleteSite () {
  const ok = window.confirm('¿Eliminar este sitio? Esta acción no se puede deshacer.');
  if (!ok) return;

  if (deleteBtn) deleteBtn.disabled = true;

    try {
      const res = await fetchWithAuth(`/api/sites/${id}`,
                                                      { method: 'DELETE' });
      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo eliminar (HTTP ${res.status})`);
      }

      displayAlert(alertSuccess, 'El sitio fue eliminado', 2500);

      setTimeout(() => navigateTo('/private/sites/table-view', true), 2000);
    } catch (err) {
      displayAlert(alertError, 'No se pudo eliminar: ' + (err.message || err), 2500);
      deleteBtn.disabled = false;
    }
}

async function updateSite() {
    const updateBtn = qs('.btn-primary');
    const cancelBtn = qs('.btn-secondary');
    const deleteBtn = qs('.btn-danger');
    if (updateBtn) updateBtn.disabled = true;
    if (cancelBtn) cancelBtn.disabled = true;
    if (deleteBtn) deleteBtn.disabled = true;
    const externalId = qs('#siteExternalId')?.value?.trim();
    const siteName = qs('#siteName')?.value?.trim();
    const siteAddress = qs('#siteAddress')?.value?.trim();
    const latStr = qs('#siteLat')?.value?.trim();
    const lonStr = qs('#siteLon')?.value?.trim();
    const siteTimeZone = qs('#siteTZ')?.value?.trim();
    const siteStatus = qs('#siteStatus')?.value;
    const siteActive = qs('#siteActive')?.checked;
    const siteLat = latStr ? Number(latStr) : null;
    const siteLon = lonStr ? Number(lonStr) : null;
    const payload = {
      name: siteName,
      address: siteAddress,
      lat: siteLat,
      lon: siteLon,
      timeZone: siteTimeZone,
      status: siteStatus,
      active: siteActive
    };
    try {
      const res = await fetchWithAuth(`/api/sites/${externalId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo guardar (HTTP ${res.status})`);
      }
      displayAlert(alertSuccess, 'Sitio actualizado correctamente', 2500);
      setTimeout(() => navigateTo('/private/sites/table-view', true), 1500);
    } catch (e) {
      displayAlert(alertError, 'No se pudo guardar: ' + (e.message || e), 2500);
      if (updateBtn) updateBtn.disabled = false;
      if (cancelBtn) cancelBtn.disabled = false;
      if (deleteBtn) deleteBtn.disabled = false;
    }
}

const cancelEditSite = () => {
    displayAlert(alertCancel, 'La edición del sitio a sido cancelada.', 2500);
    setTimeout(() => navigateTo('/private/sites/table-view', true), 2000);
}

const createZone = () => {
    const siteExternalId = qs('#siteExternalId')?.value || '';
    if (!siteExternalId){
        displayAlert(alertError, 'No existe un external ID válido para el sitio.');
        return;
    }
    navigateTo(`/private/site-zones/${siteExternalId}/zones/new`);
}

function bindEditSite() {
    const updateBtn = qs('.btn-primary');
    if (updateBtn) {
        updateBtn.addEventListener('click', updateSite);
    }
    const cancelBtn = qs('.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelEditSite);
    }
    const deleteBtn = qs('.btn-danger');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteSite);
    }
    const createZoneBtn = qs('#createZoneBtn');
    if (createZoneBtn) {
        createZoneBtn.addEventListener('click', createZone);
    }
}

async function startEditMap() {
  const mapContext = await startSiteMap();
  if (!mapContext) return;
  const { initialMarker, hasValidCoords, } = mapContext;
  enableMarkerDrag(initialMarker, (coords) => {
    qs('#siteLat').value = coords.lat;
    qs('#siteLon').value = coords.lng;
  });
  if (!hasValidCoords && initialMarker?.position) {
    const pos = initialMarker.position;
    qs('#siteLat').value = pos.lat;
    qs('#siteLon').value = pos.lng;
  }
}

/* --- init --- */
(async function init() {
  bindEditSite();
  await startEditMap();
  await loadAndRenderSiteZones();
})();
