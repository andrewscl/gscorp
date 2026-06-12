import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { initTabPanels} from '../../shared//tabs-handler.js'

const qs = (s) => document.querySelector(s);
const qsa = (s) => document.querySelectorAll(s);

const cancelViewEmployee = () => {
    // Asumiendo una ruta similar a tu ejemplo de compañías
    setTimeout(() => navigateTo('/private/employees/table-view', true), 1000);
};

function bindViewEmployee() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', cancelViewEmployee);
    }
}

// --- Inicializador ---
(async function init() {
    initTabPanels();
    
    bindViewEmployee();
})();