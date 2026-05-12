import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

const cancelViewSite = () => {
    setTimeout(() => navigateTo('/private/sites/table-view', true), 1500);
}

function bindViewSite() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewSite);
    }
}

/* --- init --- */
(function init() {
  bindViewSite();
})();