// /js/private/projects/view-project.js

document.addEventListener('DOMContentLoaded', () => {
  const backBtn = document.querySelector('.vs-btn.vs-secondary[data-path]');
  const okDiv = document.getElementById('viewProjectOk');
  const errorDiv = document.getElementById('viewProjectError');

  // Limpia mensajes de estado (por si se usan en futuro)
  if (okDiv) okDiv.style.display = 'none';
  if (errorDiv) errorDiv.textContent = '';

  // Botón "Volver al listado"
  if (backBtn) {
    backBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const path = backBtn.getAttribute('data-path');
      if (path) window.location.href = path;
    });
  }

  // Si en el futuro agregas funciones (copiar datos, imprimir, etc), puedes hacerlo aquí.
});