import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { initTabPanels } from '../../shared/tabs-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

console.log('create-employee.js cargado');

function onCancelCreate(e) {
    displayAlert(alertCancel, 'La creación del empleado ha sido cancelada.', 2000);
    navigateTo('/private/employees/table-view');
}

function bindEditEmployee() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', onCancelCreate);
    }
}

(function init() {
    initTabPanels();
    bindEditEmployee();
})();