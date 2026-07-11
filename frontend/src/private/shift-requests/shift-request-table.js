import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

const qs  = (s) => document.querySelector(s);

const createShiftRequest = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/shift-requests/create', true), 1000);
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
}

async function searchShiftRequests() {
  const from = qs('#filter-from')?.value.trim() || '';
  const to = qs('#filter-to')?.value.trim() || '';
  const siteId = qs('#filter-dept')?.value.trim() || '';

  let clientTz = '';
  try{
    clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  } catch (e) {
    clientTz = '';
  }
  const url = `/private/shift-requests/table-search?from=${from}&to=${to}&siteId=${siteId}&clientTz=${clientTz}`;
  try {
    const res = await fetchWithAuth(url, { credentials: 'same-origin'});
    if(!res.ok) throw new Error(`Error HTTP: ${res.status}`);
    const htmlResult = await res.text();
    const   tBody = qs('.hs-table-container .table tbody');
    if(tBody){
      tBody.innerHTML = htmlResult;
    }
  } catch (err) {
    console.error("No se pudo procesar la búsqueda de requerimientos de servicio:", err);
  }
}


(function init () {
  bindShiftRequestsTable();

})();