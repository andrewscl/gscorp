import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const backToShiftPatternList = () => {
    navigateTo('/private/shift-patterns/list', true);
}

function bindEvents() {
    const backBtn = qs('#cancel');
    if(backBtn) backBtn.addEventListener('click', backToShiftPatternList);
}

(function init() {
  bindEvents();
})();