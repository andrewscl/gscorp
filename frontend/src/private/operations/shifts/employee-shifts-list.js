import { navigateTo } from '../../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToUpcomingShifts = () => {
    const backshiftsBtn  = qs('#backshiftsBtn');
    if (backshiftsBtn) {
        backshiftsBtn.disabled = true;
        navigateTo('/private/shifts/employee-upcoming');
    }
}

const bindEvents = () => {
    const backshiftsBtn = qs('#backshiftsBtn');
    if (backshiftsBtn) {
        backshiftsBtn.addEventListener('click', backToUpcomingShifts);
    }
}

(function init() {
  bindEvents();
})();