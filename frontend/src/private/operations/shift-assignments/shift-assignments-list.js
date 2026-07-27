import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createShiftAssignment = () => {
    navigateTo('/private/shift-assignments/create', true);
}

const searchShiftAssignments = () => {
    navigateTo('/private/shift-assignments/search', true);
}

function bindShiftAssignmentsList() {
    const createBtn = qs('#addShiftAssignmentsBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShiftAssignment);
    }
    const searchBtn = qs('#searchShiftAssignmentsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchShiftAssignments);
    }
}

(function init () {
  bindShiftAssignmentsList();

})();