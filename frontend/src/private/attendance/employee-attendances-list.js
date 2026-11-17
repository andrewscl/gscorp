import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToEmployeeAttendancesList = () => {
    const backAttendanceBtn  = qs('#backAttendanceBtn');
    if (backAttendanceBtn) {
        backAttendanceBtn.disabled = true;
        navigateTo('/private/attendance/employee-attendances-list');
    }
};

const bindEvents = () => {
    const backAttendanceBtn = qs('#backAttendanceBtn');
    if (backAttendanceBtn) {
        backAttendanceBtn.addEventListener('click', backToEmployeeAttendancesList);
    }
}

(function init() {
  bindEvents();
})();