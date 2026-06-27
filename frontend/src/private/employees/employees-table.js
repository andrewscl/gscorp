import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

console.log('employee-table.js cargado');

const qs  = (s) => document.querySelector(s);

const createEmployee = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/employees/create', true), 1000);
}

async function searchEmployees(pageNumber = 0){
    const queryText = qs('#filter-q')?.value.trim() || '';
    const status = qs('#filter-employee-status')?.value.trim() || '';

    const pageSize = qs('#filter-employee-status')?.value.trim() || '100';

    const params = new URLSearchParams();
    if (queryText) params.append('q', queryText);
    if (status) params.append('status', status);

    params.append('page', pageNumber.toString());

    const baseUrl = '/private/employees/table-search';
    const url = params.toString() ? `${baseUrl}?${params.toString()}` : baseUrl;

    try {
        const res = await fetchWithAuth(url, { credentials: 'same-origin'});
        if(!res.ok) throw new Error(`Error HTTP: ${res.status}`);

        const htmlResult = await res.text();

        const tBody = qs('.hs-table-container .table tbody')
        if(tBody){
            tBody.innerHTML = '';
            tBody.innerHTML = htmlResult;
        }

        const hiddenCountInput = qs('#sync-employees-count');
        const headerCountSpan = qs('.count');

        if(hiddenCountInput && headerCountSpan){
            const newCount = parseInt(hiddenCountInput.value, 10) || 0;
            headerCountSpan.textContent = `${newCount} registro${newCount === 1 ? '' : 's'}`;
        }

    } catch (err) {
        console.error("No se pudo procesar la búsqueda de usuarios:", err);
    }
}


function bindEmployeesTable() {
    const addEmployeesBtn = qs('#addEmployeesBtn');
    if (addEmployeesBtn) {
        addEmployeesBtn.addEventListener('click', createEmployee);
    }
    const searchEmployeesBtn = qs('#searchEmployeesBtn');
    if (searchEmployeesBtn) {
        searchEmployeesBtn.addEventListener('click',
            () => searchEmployees(0));
    }
}

(function init () {
  bindEmployeesTable();
  initHeaderSync('.hs-table-header','--header-height');
})();