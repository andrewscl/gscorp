import { navigateTo } from '../../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToDashboard = () => {
    const backToDashboardBtn  = qs('#backToDashboardBtn');
    if (backToDashboardBtn) {
        backToDashboardBtn.disabled = true;
        navigateTo('/private/operations/dashboards/security-operator');
    }
}

const bindEvents = () => {
    const backToDashboardBtn = qs('#backToDashboardBtn');
    if (backToDashboardBtn) {
        backToDashboardBtn.addEventListener('click', backToDashboard);
    }
}

(function init() {
  bindEvents();
})();