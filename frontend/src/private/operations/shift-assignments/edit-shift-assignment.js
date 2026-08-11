import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const backToShiftAssignments = () => {
    navigateTo('/private/shift-assignments/list', true);
}

function bindEvents() {
    const backToBtn = qs('#cancel');
    if (backToBtn) {
        backToBtn.addEventListener('click', backToShiftAssignments);
    }
}

(function init () {
  bindEvents();
})();