import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const closeShiftAssignment = () => {



}

const backToShiftAssignments = () => {
    navigateTo('/private/shift-assignments/list', true);
}

function bindEvents() {
    const backToBtn = qs('#cancel');
    if (backToBtn) {
        backToBtn.addEventListener('click', backToShiftAssignments);
    }
    const closeShiftAssignmentBtn = qs('#delete');
    if (closeShiftAssignmentBtn) {
        closeShiftAssignmentBtn.addEventListener('click', closeShiftAssignment);
    }
}

(function init () {
  bindEvents();
})();