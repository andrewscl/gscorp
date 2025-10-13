export function init({ container, path }) {

  console.log('View-site.js activado');
  // Botón "Volver al listado"
  const backBtn = container.querySelector('.vs-btn.vs-secondary[data-path]');
  if (backBtn) {
    backBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const path = backBtn.getAttribute('data-path');
      if (path) window.location.href = path;
    });
  }

  // Mapa de solo visualización
  const latInput = container.querySelector('#siteLat');
  const lonInput = container.querySelector('#siteLon');
  const mapDiv = container.querySelector('#siteMap');
  if (mapDiv && latInput && lonInput && window.L) {
    // Evita inicializar dos veces si el usuario navega rápido
    if (mapDiv._leaflet_id) return;

    const lat = parseFloat(latInput.value) || -33.45;
    const lon = parseFloat(lonInput.value) || -70.66;
    console.log('Usando lat:', lat, 'lon:', lon);
    const map = L.map(mapDiv).setView([lat, lon], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    L.marker([lat, lon], { draggable: false }).addTo(map);
    console.log('Mapa inicializado OK');
  } else {
    console.log('Falta algún elemento', { mapDiv, latInput, lonInput, L: window.L });
  }

  // Si en el futuro agregas funciones (copiar datos, imprimir, exportar), puedes hacerlo aquí.
}