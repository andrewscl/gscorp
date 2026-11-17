import { navigateTo } from '../../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToEmployeeShiftsList = () => {
    const backshiftsBtn  = qs('#backshiftsBtn');
    if (backshiftsBtn) {
        backshiftsBtn.disabled = true;
        navigateTo('/private/operations/shifts/employee-shifts-list');
    }
};

const bindEvents = () => {
    const backshiftsBtn = qs('#backshiftsBtn');
    if (backshiftsBtn) {
        backshiftsBtn.addEventListener('click', backToEmployeeShiftsList);
    }
}

(function init() {
  bindEvents();
})();