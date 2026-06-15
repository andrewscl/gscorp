import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

async function onClickPatrolExecution() {
    const patrolExecutionBtn = qs('#btn-control-rondas');
    patrolExecutionBtn.disabled = true;
    setTimeout(() => navigateTo('/private/patrol-executions/execution-view', true), 1000);
}

function bindEvents() {
    qs('#btn-control-rondas')?.addEventListener('click', onClickPatrolExecution);
}

(function init() {

    bindEvents();
    setInterval(updateClock, 1000);
})();