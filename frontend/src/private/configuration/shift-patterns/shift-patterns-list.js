import { navigateTo } from "../../../navigation-handler";
import { fetchWithAuth } from "../../../auth";

const qs  = (s) => document.querySelector(s);

function createShiftPattern () {
    navigateTo('/private/shift-patterns/create', true);
}

function searchShiftPatterns () {}

function bindShiftPatternList() {
    const createBtn = qs('#addShiftPatternsBtn');
    if(createBtn) createBtn.addEventListener('click', createShiftPattern);

    const searchBtn = qs('#searchShiftPatternsBtn');
    if(searchBtn) searchBtn.addEventListener('click', searchShiftPatterns);
}

(function init () {
  bindShiftPatternList();
})();