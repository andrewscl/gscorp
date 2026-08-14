import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const navigateToCloseShiftAssignment = () => {
    const shiftAssignmentExternalId = qs('#shiftAssignmentExternalId')?.value;
    if(shiftAssignmentExternalId ) {
        const url = `/private/shift-assignments/close/${shiftAssignmentExternalId}`
        navigateTo(url, true);
    }
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
        closeShiftAssignmentBtn.addEventListener('click', navigateToCloseShiftAssignment);
    }
}

(function init () {
  bindEvents();
})();