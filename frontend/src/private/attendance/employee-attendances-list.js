import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

console.log('Employee attendances list script loaded');

const backToEmployeeAttendancesList = () => {
    const backAttendanceBtn  = qs('#backAttendanceBtn');
    if (backAttendanceBtn) {
        console.log('Navigating back to employee attendances list');
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