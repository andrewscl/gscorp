import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";

const qs  = (s) => document.querySelector(s);

const createAttendanceRecord = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/sites/create', true), 1000);
} 

function bindSiteTable() {
    const backBtn = qs('#addAttendanceBtn');
    if (backBtn) {
        backBtn.addEventListener('click', createAttendanceRecord);
    }
}

(function init () {
  bindSiteTable();

  initHeaderSync('.hs-table-header','--header-height');

})();