import { navigateTo } from '../../../navigation-handler.js';
import { fetchWithAuth } from '../../../auth.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

const updateProject = async () => {

  


      if (!name || !clientId || !startDate) {
        displayAlert(alertError, "Nombre, cliente y fecha inicio son obligatorios.", 2500);
        return;
      }

      const payload = {
        id,
        name,
        description,
        clientId,
        startDate,
        endDate,
        active
      };

      try {
        const response = await fetchWithAuth(`/api/projects/edit/${id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
          },
          body: JSON.stringify(payload)
        });

        if (!response || !response.ok) {
            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            if(response){
                const contentType = response.headers.get('content-type');
                if(contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }

        displayAlert(alertSuccess, "Proyecto actualizado correctamente.", 2500);
        setTimeout(() => navigateTo('/private/projects/list', true), 2000);

      } catch (err) {
        displayAlert(alertError, "No se pudo conectar con el servidor.", 2500);
        return;
      }

}

const cancelEditProject = () => {
    displayAlert(alertWarning, 'La edición del Proyecto a sido cancelada.', 2500);
    setTimeout(() => navigateTo('/private/projects/list', true), 2000);
}


function bindEvents () {

  const updateBtn = qs('.btn-primary');
  if (updateBtn) {
    updateBtn.addEventListener('click', updateProject);
  }
  const cancelBtn = qs('.btn-secondary');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', cancelEditProject);
  }

}


(function init() {
  bindEvents();
})();