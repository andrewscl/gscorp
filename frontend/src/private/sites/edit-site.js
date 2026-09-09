import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';
import { enableMarkerDrag } from '../../shared/maps/enable-marker-drag.js';
import { startViewMap } from './view-site.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

async function deleteSite () {
  const ok = window.confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.');
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
    const id = qs('#siteId')?.value?.trim();
    const siteName = qs('#siteName')?.value?.trim();
    const siteAddress = qs('#siteAddress')?.value?.trim();
    const siteLat = qs('#siteLat')?.value?.trim();
    const siteLon = qs('#siteLon')?.value?.trim();
    const siteTimeZone = qs('#siteTZ')?.value?.trim();
    const siteActive = qs('#siteActive')?.checked;
    const payload = {
      name: siteName,
      address: siteAddress,
      lat: siteLat,
      lon: siteLon,
      timeZone: siteTimeZone,
      active: siteActive
    };
    try {
      const res = await fetchWithAuth(`/api/sites/update/${id}`, {
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

// Geolocalización: botón para obtener lat/lon y guardar directo en backend
const getLocationBtn = document.getElementById('getLocationBtn');
const latInput = document.getElementById('siteLat');
const lonInput = document.getElementById('siteLon');
if (getLocationBtn && latInput && lonInput) {
  getLocationBtn.addEventListener('click', async () => {
    if (!navigator.geolocation) {
      alert("Geolocalización no soportada por tu navegador.");
      return;
    }
    getLocationBtn.disabled = true;
    getLocationBtn.textContent = "Obteniendo ubicación...";
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        latInput.value = position.coords.latitude;
        lonInput.value = position.coords.longitude;
        getLocationBtn.textContent = "Ubicación obtenida ✅";
        setTimeout(() => {
          getLocationBtn.textContent = "Obtener ubicación actual";
          getLocationBtn.disabled = false;
        }, 1500);
        alert(`Precisión estimada: ${Math.round(position.coords.accuracy)} metros`);
        // === GUARDAR DIRECTAMENTE EN EL BACKEND ===
        // Obtén el ID del sitio (ajusta según tu HTML)
        const id = form?.querySelector('button[type="submit"]')?.getAttribute('data-id');
        if (!id) {
          alert("No se pudo determinar el ID del sitio para guardar la ubicación.");
          return;
        }
        try {
          const res = await fetchWithAuth(`/api/sites/update-location/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              lat: position.coords.latitude,
              lon: position.coords.longitude
            }),
          });
          if (!res.ok) {
            const msg = await res.text().catch(() => '');
            throw new Error(msg || `No se pudo guardar la ubicación (HTTP ${res.status})`);
          }
          // Opcional: mostrar confirmación visual aquí
          // alert('Ubicación guardada correctamente en el sitio.');
        } catch (e) {
          alert(e.message || 'Error al guardar la ubicación en el sitio.');
        }
      },
      (error) => {
        alert("No se pudo obtener la ubicación: " + error.message);
        getLocationBtn.textContent = "Obtener ubicación actual";
        getLocationBtn.disabled = false;
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  });
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

const updateSiteZones = async () => {
    const siteExternalId = qs('#siteExternalId')?.value || '';
    if (!siteExternalId){
        displayAlert(alertError, 'No existe un external ID válido para el sitio.');
        return;
    }
    try {
      const url = `/api/v1/site-zones/list?siteExternalId=${encodeURIComponent(siteExternalId)}`;
      const res = await fetchWithAuth(url, {
        method: 'GET',
        headers: {'Accept': 'application/json'},
      });
      if (!res || !res.ok) {
        let errorMessage = 'Ocurrió un problema al conseguir las siteZones.';
        if (res){
          const contentType = res.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await res.json();
            errorMessage = errorData.message || errorMessage;
          }
        }
        displayAlert(alertError, `Error: ${errorMessage}`);
        return;
      }
      const siteZones = await res.json();
      const tbody = qs('#site-zones-body');
      const container = qs('#site-zones-container');
      const emptyMsg = qs('#no-site-zones-msg');
      tbody.innerHTML = '';
      if (Array.isArray(siteZones) && siteZones.length > 0) {
        if(tbody){
          siteZones.forEach(siteZone => {
          const tr = document.createElement('tr');
          tr.innerHTML = `
                          <td>${siteZone.name || '-'}</td>
                          <td>${siteZone.status.displayName || '-'}</td>
                        `;
          tbody.appendChild(tr);
          });
        }
        if(container) container.style.display = 'block';
        if(emptyMsg) emptyMsg.style.display = 'none'
      } else {
        if(container) container.style.display = 'none';
        if(emptyMsg) emptyMsg.style.display = 'block'
      }
    } catch (error) {
        console.error('Error al actualizar zonas del sitio: ', error);
        displayAlert(alertError, 'Ocurrió un error al cargar las zonas del sitio', 3000);
    }
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

function startEditMap() {
  startViewMap().then(async (result) => {
    if (result) {
      const { map, siteData, initialMarker } = result;
      enableMarkerDrag(initialMarker, (coords) => {
        const position = initialMarker.position;

        // extraer coordenadas
        const newLat = position.lat;
        const newLon = position.lng;
        // actualizar inputs
        qs('#siteLat').value = newLat;
        qs('#siteLon').value = newLon;
      });
    }
  });
}

/* --- init --- */
(function init() {
  bindEditSite();
  startEditMap();
  updateSiteZones();
})();
