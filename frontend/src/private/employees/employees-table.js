import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

const qs  = (s) => document.querySelector(s);

const createEmployee = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/employees/create', true), 1000);
}

async function searchEmployees () {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/employees/create', true), 1000);
}

function bindEmployeesTable() {
    const addEmployeesBtn = qs('#addEmployeesBtn');
    const searchEmployeesBtn = qs('#searchEmployeesBtn');
    if (addEmployeesBtn) {
        addEmployeesBtn.addEventListener('click', createEmployee);
    }
    if (searchEmployeesBtn) {
        searchEmployeesBtn.addEventListener('click', searchEmployees);
    }
}

(function init () {
  bindEmployeesTable();
  initHeaderSync('.hs-table-header','--header-height');
})();