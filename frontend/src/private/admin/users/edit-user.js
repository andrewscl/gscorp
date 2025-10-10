import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';

const btn = document.querySelector('#deleteUserBtn');
if (btn) {
  btn.addEventListener('click', async () => {
    const id = btn.getAttribute('data-id');
    if (!id) return;

    const ok = window.confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.');
    if (!ok) return;

    btn.disabled = true;

    try {
      const res = await fetchWithAuth(`/api/users/${id}`, { method: 'DELETE' });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo eliminar (HTTP ${res.status})`);
      }

      // vuelta al listado
      navigateTo('/private/users/table-view', true);
    } catch (e) {
      alert(e.message || 'Error al eliminar el usuario.');
      btn.disabled = false;
    }
  });
}
