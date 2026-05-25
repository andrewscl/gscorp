import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

const backToPatrolList = () => {
    setTimeout(() => navigateTo('/private/patrols/table-view', true), 1000);
}

function bindViewPatrol() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', backToPatrolList);
    }
}

(function init() {
    bindViewPatrol();
})();