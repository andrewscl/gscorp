import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const backEvents = () => {
    navigateTo('/private/shift-assignments/list', true);
}

function bindEvents() {
    const backToBtn = qs('#searchShiftAssignmentsBtn');
    if (backToBtn) {
        backToBtn.addEventListener('click', backToShiftAssignments);
    }
}

(function init () {
  bindEvents();
})();