import { navigateTo } from '../../navigation-handler.js';
import { loadAndRenderSiteZones } from './../operations/site-zones/site-zones-service.js';
import { startSiteMap } from './start-sites-map.js';

const qs  = (s) => document.querySelector(s);

const cancelViewSite = () => {
  navigateTo('/private/sites/table-view', 1000);
}

function bindViewSite() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewSite);
    }
}

(async function init() {
  bindViewSite();
  console.log('Initializing view site page...');
  await startSiteMap();
  await loadAndRenderSiteZones();
})();
