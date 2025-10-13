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

  // Mapa de solo visualización
  const latInput = document.getElementById('siteLat');
  const lonInput = document.getElementById('siteLon');
  const mapDiv = document.getElementById('siteMap');
  if (mapDiv && latInput && lonInput && window.L) {
    const lat = parseFloat(latInput.value) || -33.45;
    const lon = parseFloat(lonInput.value) || -70.66;
    const map = L.map(mapDiv).setView([lat, lon], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    L.marker([lat, lon], { draggable: false }).addTo(map);
  }

  // Si en el futuro agregas funciones (copiar datos, imprimir, exportar), puedes hacerlo aquí.
});