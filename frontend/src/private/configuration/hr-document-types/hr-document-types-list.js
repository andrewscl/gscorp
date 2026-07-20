import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createHrDocumentType = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/employees/transition-requests/create-termination-request', true);
}

const searchHrDocumentType = async (btn) => {
    if (btn) btn.disabled = true;

}

function bindHrDocumentTypesList() {
    const createBtn = qs('#addTerminationRequest');
    if(createBtn) createBtn.addEventListener('click', createHrDocumentType);

    const searchBtn = qs('#searchShiftRequestsBtn');
    if(searchBtn) searchBtn.addEventListener('click', searchHrDocumentType);
}

(function init () {
  bindHrDocumentTypesList();

})();