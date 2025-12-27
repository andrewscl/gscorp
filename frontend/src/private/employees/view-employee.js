import { navigateTo } from '../../navigation-handler.js';

console.log('view-employee.js cargado');

// Helper to select elements
const qs = (s) => document.querySelector(s);

/* --- Manejo del botón regresar --- */
function onCancelView(e) {
  e.preventDefault();
  navigateTo('/private/employees/table-view'); // Redirige a la tabla de empleados
}

/* --- Vínculos y acciones --- */
function bindViewEmployee() {
  qs('#cancelViewEmployee')?.addEventListener('click', onCancelView);
}

/* --- Inicialización --- */
(function init() {
  bindViewEmployee(); // Víncula los botones y eventos
})();