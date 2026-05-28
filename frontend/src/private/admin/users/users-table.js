import { initHeaderSync } from "../../../shared/sync-header-height";
import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const inviteUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/invite', true), 1000);
}

const createUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/create', true), 1000);
}

async function searchUser(){
    const queryText = qs('#filter-q')?.value.trim() || '';
    const status = qs('#filter-user-status')?.value.trim() || '';

    const params = new URLSearchParams();

    if (queryText) params.append('q', queryText);
    if (status) params.append('status', status);

    //Agregar paginación por si se requiere controlar en el futuro
    params.append('page', '0');
    params.append('size', '100');

    //ensamblar url
    const baseUrl = '/private/users/table-search';
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
    } catch (err) {
        console.error("No se pudo procesar la búsqueda de usuarios:", err);
    }
}

function bindUserTable() {
    const addUserBtn = qs('#addUserBtn');
    const inviteUserBtn = qs('#inviteUserBtn');
    const searchUserBtn = qs('#searchUserBtn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', createUser);
    }
    if (inviteUserBtn) {
        inviteUserBtn.addEventListener('click', inviteUser);
    }
    if (searchUserBtn) {
        searchUserBtn.addEventListener('click', searchUser);
    }
}

(function init () {
  bindUserTable();

  initHeaderSync('.hs-table-header','--header-height');

})();