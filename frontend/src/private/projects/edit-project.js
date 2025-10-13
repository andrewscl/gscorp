// /js/private/projects/edit-project.js

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('editProjectForm');
  const errorDiv = document.getElementById('editProjectError');
  const okDiv = document.getElementById('editProjectOk');
  const cancelBtn = document.querySelector('.vs-btn.vs-secondary[data-path]');

  // Helper to clear messages
  const clearMessages = () => {
    errorDiv.textContent = '';
    okDiv.style.display = 'none';
  };

  // Cancel button: go back to table view
  if (cancelBtn) {
    cancelBtn.addEventListener('click', (e) => {
      e.preventDefault();
      window.location.href = cancelBtn.getAttribute('data-path');
    });
  }

  // Form submission
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearMessages();

      // Gather form data
      const id = document.querySelector('.meta-id span')?.textContent?.trim();
      const name = form.projectName.value.trim();
      const description = form.projectDescription.value.trim();
      const clientId = form.projectClient.value;
      const startDate = form.projectStartDate.value;
      const endDate = form.projectEndDate.value;
      const active = form.projectActive.checked;

      // Basic validation
      if (!name || !clientId || !startDate) {
        errorDiv.textContent = "Nombre, cliente y fecha inicio son obligatorios.";
        return;
      }

      // Build payload
      const payload = {
        id,
        name,
        description,
        clientId,
        startDate,
        endDate,
        active
      };

      // Send the request (assume REST API endpoint, e.g. /api/projects/edit/{id})
      try {
        const response = await fetch(`/api/projects/edit/${id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
          },
          body: JSON.stringify(payload)
        });

        if (response.ok) {
          okDiv.style.display = '';
          okDiv.textContent = 'Cambios guardados ✅';
          // Optionally redirect after a delay
          setTimeout(() => {
            window.location.href = '/private/projects/table-view';
          }, 1000);
        } else {
          const data = await response.json().catch(() => ({}));
          errorDiv.textContent = data.message || "Error al guardar los cambios.";
        }
      } catch (err) {
        errorDiv.textContent = "No se pudo conectar con el servidor.";
      }
    });
  }
});