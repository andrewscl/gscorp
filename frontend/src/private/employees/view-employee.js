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


function setHeaderHeight() {
  const header = document.querySelector('.viewEmployeeHeaderCard'); // Selecciona el encabezado

  if (header) {
    // Obtén la altura del encabezado dinámicamente
    const headerHeight = header.offsetHeight;

    // Setea la variable CSS en el :root
    document.documentElement.style.setProperty('--header-height', `${headerHeight}px`);
  }
}


// Llamar al ajuste después de que se renderice el DOM
window.addEventListener('resize', setHeaderHeight); // Recalcular en caso de redimensionar la ventana



/* --- Inicialización --- */
(function init() {
  bindViewEmployee(); // Víncula los botones y eventos
  setHeaderHeight();
})();