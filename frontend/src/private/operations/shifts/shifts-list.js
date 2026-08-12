import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createShift = () => {
    navigateTo('/private/shifts/create', true);
}

const searchShifts = () => {
    navigateTo('/private/shifts/search', true);
}

function bindEvents() {
    const createBtn = qs('#addShiftsBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createShift);
    }
    const searchBtn = qs('#searchshiftsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchShifts);
    }
}

(function init () {
    bindEvents();
})();