import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToShiftRequestsTable = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/shift-requests/table-view', true), 1000);
}

function bindViewShiftRequest() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', backToShiftRequestsTable);
    }
}

(async function init() {
  bindViewShiftRequest();
  console.log('Initializing view shift-requests page...');

})();