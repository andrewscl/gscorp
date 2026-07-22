import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { qs } from "../../../shared/dom-utils";

const cancelViewEmployeeTerminationRequest = () => {
        navigateTo('/private/employee-terminations/list');
}

function bindFunctions() {
    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', cancelViewEmployeeTerminationRequest);
}

(function init () {
    bindFunctions();
})();