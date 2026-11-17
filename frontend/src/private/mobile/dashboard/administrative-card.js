import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

const bindEvents = () => {
    const scanBtn = qs('.btn-scan');
    if (scanBtn) {
        scanBtn.addEventListener('click', scanBtnExecute);
    }
};

const HumanResourcesHistory = () => {
    const attendanceHistoryBtn  = qs('.btn-rrhh-history');
    if (attendanceHistoryBtn) {
        attendanceHistoryBtn = true;
        navigateTo('/private/attendance/employee-list');
    }
};

const attendanceHistoryBtn = qs('.btn-rrhh-history');
if (attendanceHistoryBtn) {
    attendanceHistoryBtn.addEventListener('click', HumanResourcesHistory);
}

(function init() {
    bindEvents();
})();