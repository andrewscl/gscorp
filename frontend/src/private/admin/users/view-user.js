import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';

function bindViewSite() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewSite);
    }
}

(async function init() {
    bindViewSite();
})();