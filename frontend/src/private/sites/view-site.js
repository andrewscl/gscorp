// /js/private/sites/view-site.js

document.addEventListener('DOMContentLoaded', () => {
  // Botón "Volver al listado"
  const backBtn = document.querySelector('.vs-btn.vs-secondary[data-path]');
  if (backBtn) {
    backBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const path = backBtn.getAttribute('data-path');
      if (path) window.location.href = path;
    });
  }

  // Si en el futuro agregas funciones (copiar datos, imprimir, exportar), puedes hacerlo aquí.
});