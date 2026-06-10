import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

const qs  = (s) => document.querySelector(s);

const createClient = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/clients/create', true), 1000);
}

async function searchClients () {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/companies/create', true), 1000);
}

function bindClientsTable() {
    const addClientsBtn = qs('#addClientsBtn');
    const searchClientsBtn = qs('#searchClientsBtn');
    if (addClientsBtn) {
        addClientsBtn.addEventListener('click', createClient);
    }
    if (searchClientsBtn) {
        searchClientsBtn.addEventListener('click', searchClients);
    }
}

(function init () {
  bindClientsTable();

  initHeaderSync('.hs-table-header','--header-height');

})();