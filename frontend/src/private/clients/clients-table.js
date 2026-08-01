import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

const qs  = (s) => document.querySelector(s);

const createClient = () => {
    navigateTo('/private/clients/create', true);
}

async function searchClients () {
    setTimeout(() => navigateTo('/private/companies/create', true), 1000);
}

function bindClientsTable() {
    const addClientsBtn = qs('#addClientsBtn');
    if (addClientsBtn) {
        addClientsBtn.addEventListener('click', createClient);
    }
    const searchClientsBtn = qs('#searchClientsBtn');
    if (searchClientsBtn) {
        searchClientsBtn.addEventListener('click', searchClients);
    }
}

(function init () {
  bindClientsTable();
})();