import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

const AttendancesHistory = () => {
    const attendanceHistoryBtn  = qs('#btn-rrhh-history');
    if (attendanceHistoryBtn) {
        attendanceHistoryBtn.disabled = true;
        navigateTo('/private/attendance/employee-list');
    }
};

const EmployeeSchedule = () => {
    const shiftScheduleBtn  = qs('#btn-rrhh-schedule');
    if (shiftScheduleBtn) {
        shiftScheduleBtn.disabled = true;
        navigateTo('/private/shifts/employee-upcoming');
    }
};

const bindEvents = () => {
    const attendanceHistoryBtn = qs('#btn-rrhh-history');
    if (attendanceHistoryBtn) {
        attendanceHistoryBtn.addEventListener('click', AttendancesHistory);
    }
    const shiftScheduleBtn = qs('#btn-rrhh-schedule');
    if (shiftScheduleBtn) {
        shiftScheduleBtn.addEventListener('click', EmployeeSchedule);
    }
}


(function init() {
    bindEvents();
})();