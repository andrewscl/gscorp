import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createTransitionRequest = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/employees/transition-requests/create-termination-request', true);
}

const searchTransitionRequest = async (btn) => {
    if (btn) btn.disabled = true;

}

function bindEmployeeTransitionRequestsList() {
    const createBtn = qs('#addTerminationRequest');
    if(createBtn) createBtn.addEventListener('click', createTransitionRequest);

    const searchBtn = qs('#searchShiftRequestsBtn');
    if(searchBtn) searchBtn.addEventListener('click', searchTransitionRequest);
}

(function init () {
  bindEmployeeTransitionRequestsList();

})();