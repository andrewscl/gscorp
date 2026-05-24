import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

const qs  = (s) => document.querySelector(s);

const createAttendanceRecord = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/attendance/create', true), 1000);
}

function bindAttendanceTable() {
    const createAttendanceBtn = qs('#addAttendanceBtn');
    if (createAttendanceBtn) {
        createAttendanceBtn.addEventListener('click', createAttendanceRecord);
    }
    const searchAttendanceBtn = qs('#searchAttendanceBtn');
    if (searchAttendanceBtn) {
        searchAttendanceBtn.addEventListener('click', searchAttendance);
    }
}

async function searchAttendance() {
  const from = qs('#filter-from')?.value || '';
  const to = qs('#filter-to')?.value || '';
  const siteId = qs('#filter-dept')?.value || '';
  const actionName = qs('#filter-name')?.value || '';

  let clientTz = '';
  try{
    clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  } catch (e) {
    clientTz = '';
  }
  const url = `/private/attendance/table-search?from=${from}&to=${to}&siteId=${siteId}&action=${encodeURIComponent(actionName)}&clientTz=${clientTz}`;
  try {
    const res = await fetchWithAuth(url, { credentials: 'same-origin'});
    if(!res.ok) throw new Error(`Error HTTP: ${res.status}`);
    const htmlResult = await res.text();
    const tableContainer = qs('.hs-table-container .table tbody');
    if(tableContainer){
      tableContainer.innerHtml = htmlResult;
    }
  } catch (err) {
    console.error("No se pudo procesar la búsqueda de asistencias:", err);
  }
}



(function init () {
  bindAttendanceTable();
  searchAttendance();
  initHeaderSync('.hs-table-header','--header-height');

})();