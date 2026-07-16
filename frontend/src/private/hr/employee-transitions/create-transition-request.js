import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { fetchWithAuth } from "../../../shared/dom-utils";

const cancelTransitionRequest = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/employees/transition-requests/list', true);
}

const createTransitionRequest = (btn) => {
    if (btn) btn.disabled = true;
    
}

function bindFunctions() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', createTransitionRequest);

    const cancelBtn = qs('#cancel');
    if(cancelBtn) searchBtn.addEventListener('click', cancelTransitionRequest);
}

(function init () {
  bindFunctions();

})();