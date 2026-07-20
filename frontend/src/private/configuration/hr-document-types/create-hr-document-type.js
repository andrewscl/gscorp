import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";
import { qs } from "../../../shared/dom-utils";

const cancelHrDocumentType = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/hr/termination-requests/views/termination-requests-list', true);
}

const createHrDocumentType = (btn) => {
    if (btn) btn.disabled = true;
    navigateTo('/private/hr/termination-requests/views/create-termination-requests-list', true);
}

function bindFunctions() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', createHrDocumentType);

    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', cancelHrDocumentType);
}

(function init () {
  bindFunctions();

})();