import { navigateTo } from "../../../navigation-handler.js";
import { fetchWithAuth } from "../../../auth.js";

const qs  = (s) => document.querySelector(s);

const createCompany = () => {
    navigateTo('/admin/companies/create', true);
}

async function searchCompanies () {
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
})();