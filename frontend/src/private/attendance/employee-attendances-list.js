import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToDashboard = () => {
    const backAttendanceBtn  = qs('#backToDashboardBtn');
    if (backAttendanceBtn) {
        backAttendanceBtn.disabled = true;
        navigateTo('/private/operations/dashboards/security-operator');
    }
}

const bindEvents = () => {
    const backAttendanceBtn = qs('#backToDashboardBtn');
    if (backAttendanceBtn) {
        backAttendanceBtn.addEventListener('click', backToDashboard);
    }
}

(function init() {
  bindEvents();
})();