import { initHeaderSync } from "../../../shared/sync-header-height";
import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createCompany = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/companies/create', true), 1000);
}

async function searchCompanies () {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/companies/create', true), 1000);
}

function bindCompaniesTable() {
    const addCompaniesBtn = qs('#addCompaniesBtn');
    const searchCompaniesBtn = qs('#searchCompaniesBtn');
    if (addCompaniesBtn) {
        addCompaniesBtn.addEventListener('click', createCompany);
    }
    if (searchCompaniesBtn) {
        searchCompaniesBtn.addEventListener('click', searchCompanies);
    }
}

(function init () {
  bindCompaniesTable();

  initHeaderSync('.hs-table-header','--header-height');

})();