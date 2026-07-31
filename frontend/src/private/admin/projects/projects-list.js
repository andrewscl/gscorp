import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

const createProject = () => {
    navigateTo('/private/projects/create', true);
}

const searchProjects = () => {
    navigateTo('/private/projects/search', true);
}

function bindEvents() {
    const createBtn = qs('#addprojectsBtn');
    if (createBtn) {
        createBtn.addEventListener('click', createProject);
    }
    const searchBtn = qs('#searchProjectsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchProjects);
    }
}

(function init () {
  bindEvents();

})();