import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

async function onClickPatrolDashboard() {
    const patrolExecutionBtn = qs('#btn-control-rondas');
    patrolExecutionBtn.disabled = true;
    setTimeout(() => navigateTo('/private/patrols/patrol-dashboard', true), 1000);
}

function bindEvents() {
    qs('#btn-control-rondas')?.addEventListener('click', onClickPatrolDashboard);
}

(function init() {

    bindEvents();
})();