import { initHeaderSync } from "../../shared/sync-header-height";
import { navigateTo } from "../../navigation-handler";
import { fetchWithAuth } from "../../auth";

console.log('employee-table.js cargado');

const qs  = (s) => document.querySelector(s);

const createEmployee = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/employees/create', true), 1000);
}

async function searchEmployees () {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/employees/create', true), 1000);
}


async function searchEmployee(){
    const queryText = qs('#filter-q')?.value.trim() || '';
    const status = qs('#filter-employee-status')?.value.trim() || '';
    const count = qs('#count')?.value.trim() || '';

    const params = new URLSearchParams();
    if (queryText) params.append('q', queryText);
    if (status) params.append('status', status);
    //Agregar paginación por si se requiere controlar en el futuro
    params.append('page', '0');
    params.append('size', '100');
    //ensamblar url
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

        const hiddenCountInput = qs('#sync-users-count');
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