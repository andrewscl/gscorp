import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const deleteBtn = document.querySelector('#deleteSiteBtn');
if (deleteBtn) {
  deleteBtn.addEventListener('click', async () => {
    const id = deleteBtn.getAttribute('data-id');
    if (!id) return;

    const ok = window.confirm('¿Eliminar este sitio? Esta acción no se puede deshacer.');
    if (!ok) return;

    deleteBtn.disabled = true;

    try {
      const res = await fetchWithAuth(`/api/sites/${id}`, { method: 'DELETE' });

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
const form = document.querySelector('#editSiteForm');
if (form) {
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();

    const id = form.querySelector('button[type="submit"]').getAttribute('data-id');
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
      const res = await fetchWithAuth(`/api/sites/${id}`, {
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