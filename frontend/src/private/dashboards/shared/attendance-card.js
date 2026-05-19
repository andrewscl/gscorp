import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

async function onClickNavigate() {
    const attendanceBtn = qs('#attendance-card-btn');
    attendanceBtn.disabled = true;
    setTimeout(() => navigateTo('/private/attendance/attdc-view', true), 1000);
}

function bindEvents() {
    qs('#attendance-card-btn')?.addEventListener('click', onClickNavigate);
}

(function init() {
    bindEvents();
})();   