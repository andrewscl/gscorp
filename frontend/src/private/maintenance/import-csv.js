import { fetchWithAuth } from '../../auth.js';

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('importCsvForm');
  const status = document.getElementById('importStatus');
  const cancelBtn = document.getElementById('cancelImportBtn');

  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    status.textContent = 'Importando archivo...';
    status.className = '';

    const fileInput = form.querySelector('input[type="file"][name="file"]');
    const file = fileInput.files[0];

    if (!file) {
      status.textContent = 'Selecciona un archivo CSV.';
      status.className = 'is-warning';
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      const resp = await fetchWithAuth('/api/employees/import-csv', {
        method: 'POST',
        body: formData
      });
      const text = await resp.text();

      if (resp.ok) {
        status.textContent = text || '¡Importación exitosa!';
        status.className = 'is-success';
        form.reset();
      } else {
        status.textContent = `Error: ${text}`;
        status.className = 'is-error';
      }
    } catch (err) {
      status.textContent = 'Error de red o servidor.';
      status.className = 'is-error';
    }
  });

  if (cancelBtn) {
    cancelBtn.addEventListener('click', (e) => {
      e.preventDefault();
      form.reset();
      status.textContent = '';
      status.className = '';
      form.style.display = 'none';
    });
  }
});