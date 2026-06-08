import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

function updateClock () {

    const hoursMinsElement = qs("#card-hours-mins");
    const secondsElement = qs("#card-seconds");
    
    const now = new Date();

    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    if(hoursMinsElement && secondsElement) {
        hoursMinsElement.textContent = `${hours}:${minutes}`;
        secondsElement.textContent = `:${seconds}`;
    }
}


async function onClickNavigate() {
    const attendanceBtn = qs('#attendance-card-btn');
    attendanceBtn.disabled = true;
    setTimeout(() => navigateTo('/private/attendance/attdc-view', true), 1000);
}


function bindEvents() {
    qs('#attendance-card-btn')?.addEventListener('click', onClickNavigate);
}


export async function getLastPunch() {
    const res = await fetchWithAuth('/api/attendance/last-punch');
}


(function init() {

    bindEvents();

    setInterval(updateClock(), 1000);
})();

