import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

const HumanResourcesHistory = () => {
    const attendanceHistoryBtn  = qs('#btn-rrhh-history');
    if (attendanceHistoryBtn) {
        attendanceHistoryBtn.disabled = true;
        navigateTo('/private/attendance/employee-list');
    }
};

const HumanResourcesSchedule = () => {
    const shiftScheduleBtn  = qs('#btn-rrhh-schedule');
    if (shiftScheduleBtn) {
        shiftScheduleBtn.disabled = true;
        navigateTo('/private/operations/shifts/views/employee-shifts-list');
    }
};

const bindEvents = () => {
    const attendanceHistoryBtn = qs('#btn-rrhh-history');
    if (attendanceHistoryBtn) {
        attendanceHistoryBtn.addEventListener('click', HumanResourcesHistory);
    }
    const shiftScheduleBtn = qs('#btn-rrhh-schedule');
    if (shiftScheduleBtn) {
        shiftScheduleBtn.addEventListener('click', HumanResourcesSchedule);
    }
}


(function init() {
    bindEvents();
})();