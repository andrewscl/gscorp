import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// Eliminar sitio
const deleteBtn = document.querySelector('#deleteSiteBtn');
if (deleteBtn) {
  deleteBtn.addEventListener('click', async () => {
    const id = deleteBtn.getAttribute('data-id');
    if (!id) return;

    const ok = window.
          confirm('¿Eliminar este sitio? Esta acción no se puede deshacer.');
    if (!ok) return;

    deleteBtn.disabled = true;

    try {
      const res = await fetchWithAuth(`/api/sites/${id}`,
                                                      { method: 'DELETE' });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo eliminar (HTTP ${res.status})`);
      }

      // vuelta al listado
      navigateTo('/private/sites/table-view', true);
    } catch (e) {
      alert(e.message || 'Error al eliminar el sitio.');
      deleteBtn.disabled = false;
    }
  });
}

// Guardar cambios
const form = document.getElementById('editSiteForm');
if (form) {
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();

    const id = form.querySelector('button[type="submit"]')?.getAttribute('data-id');
    if (!id) return;

    const fd = new FormData(form);
    // Convierte a objeto plano
    const data = {};
    fd.forEach((value, key) => {
      if (key === 'active') {
        data[key] = form.querySelector('#siteActive').checked;
      } else {
        data[key] = value;
      }
    });

    const errorDiv = document.getElementById('editSiteError');
    const okDiv = document.getElementById('editSiteOk');
    errorDiv.textContent = '';
    okDiv.style.display = 'none';

    try {
      const res = await fetchWithAuth(`/api/sites/update/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo guardar (HTTP ${res.status})`);
      }

      okDiv.style.display = '';
      navigateTo('/private/sites/table-view', true);
    } catch (e) {
      errorDiv.textContent = e.message || 'Error al guardar el sitio.';
    }
  });
}

// Geolocalización: botón para obtener lat/lon y guardar directo en backend
const getLocationBtn = document.getElementById('getLocationBtn');
const latInput = document.getElementById('siteLat');
const lonInput = document.getElementById('siteLon');

if (getLocationBtn && latInput && lonInput) {
  getLocationBtn.addEventListener('click', async () => {
    if (!navigator.geolocation) {
      alert("Geolocalización no soportada por tu navegador.");
      return;
    }

    getLocationBtn.disabled = true;
    getLocationBtn.textContent = "Obteniendo ubicación...";

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        latInput.value = position.coords.latitude;
        lonInput.value = position.coords.longitude;
        getLocationBtn.textContent = "Ubicación obtenida ✅";
        setTimeout(() => {
          getLocationBtn.textContent = "Obtener ubicación actual";
          getLocationBtn.disabled = false;
        }, 1500);
        alert(`Precisión estimada: ${Math.round(position.coords.accuracy)} metros`);

        // === GUARDAR DIRECTAMENTE EN EL BACKEND ===
        // Obtén el ID del sitio (ajusta según tu HTML)
        const id = form?.querySelector('button[type="submit"]')?.getAttribute('data-id');
        if (!id) {
          alert("No se pudo determinar el ID del sitio para guardar la ubicación.");
          return;
        }

        try {
          const res = await fetchWithAuth(`/api/sites/update-location/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              lat: position.coords.latitude,
              lon: position.coords.longitude
            }),
          });
          if (!res.ok) {
            const msg = await res.text().catch(() => '');
            throw new Error(msg || `No se pudo guardar la ubicación (HTTP ${res.status})`);
          }
          // Opcional: mostrar confirmación visual aquí
          // alert('Ubicación guardada correctamente en el sitio.');
        } catch (e) {
          alert(e.message || 'Error al guardar la ubicación en el sitio.');
        }
      },
      (error) => {
        alert("No se pudo obtener la ubicación: " + error.message);
        getLocationBtn.textContent = "Obtener ubicación actual";
        getLocationBtn.disabled = false;
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  });
}