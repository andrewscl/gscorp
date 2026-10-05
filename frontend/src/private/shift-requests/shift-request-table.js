import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";
import { populateSelect } from "../../shared/insert-select";

const qs  = (s) => document.querySelector(s);

const createShiftRequest = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/shift-requests/create', true), 1000);
}

async function searchShiftRequests() {
  const from = qs('#filter-from')?.value.trim() || '';
  const to = qs('#filter-to')?.value.trim() || '';
  const siteExternalId = qs('#siteFilter')?.value.trim() || '';
  const siteZoneExternalId = qs('#siteZoneFilter')?.value.trim() || '';

  let clientTz = '';
  try{
    clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  } catch (e) {
    clientTz = '';
  }
  const url = `/private/shift-requests/table-search?from=${from}&to=${to}&siteExternalId=${siteExternalId}&siteZoneExternalId=${siteZoneExternalId}&clientTz=${clientTz}`;
  try {
        const res = await fetchWithAuth(url, { credentials: 'same-origin'});
        if(!res.ok) throw new Error(`Error HTTP: ${res.status}`);
        const htmlResult = await res.text();
        const   tBody = qs('.hs-table-container .table tbody');
        if(tBody){
          tBody.innerHTML = htmlResult;
        }

        const hiddenCountInput = qs('#sync-shift-request-count');
        const headerCountSpan = qs('.count');
        if(hiddenCountInput && headerCountSpan){
            const newCount = parseInt(hiddenCountInput.value, 10) || 0;
            headerCountSpan.textContent = `${newCount} registro${newCount === 1 ? '' : 's'}`;
        }

  } catch (err) {
    console.error("No se pudo procesar la búsqueda de requerimientos de servicio:", err);
  }
}

async function handleSiteChange() {
  const siteFilterSelect = qs('#siteFilter');
  const siteExternalId = siteFilterSelect?.value;
  const siteZoneFilterSelect = qs('#siteZoneFilter');
  if (!siteExternalId) {
      populateSelect({selectEl: siteZoneFilterSelect, items: [], emptyLabel: 'Primero seleccione un sitio.'});
      return;
  }
  try {
    const url = `/api/v1/site-zones/site/${siteExternalId}/site-zones`;
    const res = await fetchWithAuth(url, {
                          method: 'GET',
                          headers: {'Accept': 'application/json'}
    });
    if(!res || !res.ok) throw new Error('No se pudieron obtener las zonas del sitio seleccionado.');
    const siteZones = await res.json();
    populateSelect({
      selectEl: siteZoneFilterSelect,
      items: siteZones,
      defaultLabel: 'Seleccione una ubicación',
      emptyLabel: 'Sin ubicaciones asociadas.'});
    } catch (err){
    console.error('Error en HandleSiteChange:', err);
    populateSelect({selectEl: siteFilterSelect, items: [], emptyLabel: 'Error al cargar las ubicaciones.'});
    }
}

function bindShiftRequestsTable() {
    const createShiftRequestBtn = qs('#addShiftRequestsBtn');
    if (createShiftRequestBtn) {
        createShiftRequestBtn.addEventListener('click', createShiftRequest);
    }
    const searchShiftRequestsBtn = qs('#searchShiftRequestsBtn');
    if (searchShiftRequestsBtn) {
        searchShiftRequestsBtn.addEventListener('click', searchShiftRequests);
    }
    const siteFilterSelect = qs('#siteFilter');
    if (siteFilterSelect) {
        siteFilterSelect.addEventListener('change', handleSiteChange);
    }
}

(function init () {
  bindShiftRequestsTable();

})();