import { initHeaderSync } from "../../../shared/sync-header-height";
import { navigateTo } from "../../../navigation-handler";

const qs  = (s) => document.querySelector(s);

const inviteUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/invite', true), 1000);
}

const createUser = (e) => {
    e.target.disabled = true;
    setTimeout(() => navigateTo('/private/users/create', true), 1000);
} 

function bindUserTable() {
    const addUserBtn = qs('#addUserBtn');
    const inviteUserBtn = qs('#inviteUserBtn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', createUser);
    }
    if (inviteUserBtn) {
        inviteUserBtn.addEventListener('click', inviteUser);
    }
}

(function init () {
  bindUserTable();

  initHeaderSync('.hs-table-header','--header-height');

})();