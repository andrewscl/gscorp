import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../navigation-handler.js';
import { displayAlert } from '../../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);

const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');

async function onCreateShiftPattern() {
    const createBtn = qs('#submit');
    const cancelBtn = qs('#cancel');

    const name = qs('#shiftPatternName')?.value?.trim();
    const code = qs('#shiftPatternCode')?.value?.trim() || null;
    const description = qs('#shiftPatternDescription')?.value?.trim() || null;
    const workDaysStr = qs('#shiftPatternWorkDays')?.value?.trim();
    const restDaysStr = qs('#shiftPatternRestDays')?.value?.trim();

    const workDays = workDaysStr ? Number(workDaysStr) : null;
    const restDays = restDaysStr ? Number(restDaysStr) : null;

    if (!name) {
          displayAlert(alertError, 'El nombre es obligatorio.', 2000);
          return;
    }
    if (!workDays || isNaN(workDays) || workDays < 1) {
          displayAlert(alertError, 'Los días de trabajo son obligatorios y deben ser un número mayor a 0.', 2000);
          return;
    }
    if (restDays === null || isNaN(restDays) || restDays < 0) {
          displayAlert(alertError, 'Los días de descanso son obligatorios y deben ser 0 o más.', 2000);
          return;
    }
    const payload = {name: name,
                      code: name,
                      description: description,
                      workDays: workDays,
                      restDays: restDays};
    createBtn.disabled = true;
    cancelBtn.disabled = true;

  try {
        const res = await fetchWithAuth('/api/shift-patterns/create', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (!res || !res.ok) {
            createBtn.disabled = false;
            cancelBtn.disabled = false;
            let errorMessage = 'Ocurrió un problema al enviar el formulario.';
            if(res){
                const contentType = res.headers.get('content-type');
                if(contentType && contentType.includes('application/json')) {
                    const errorData = await res.json();
                    errorMessage = errorData.message || errorMessage;
                }
            }
            displayAlert(alertError, `Error: ${errorMessage}`);
            return;
        }

        displayAlert(alertSuccess, 'El sistema de jornada ha sido creado correctamente.', 2000);
        setTimeout(() => {
            navigateTo('/private/shift-patterns/list', true);
        }, 2000);

  } catch (error) {
        console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
        displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
        createBtn.disabled = false;
        cancelBtn.disabled = false;
  }
}

const onCancelShiftPattern = () => {
    navigateTo('/private/shift-patterns/list', true);
}

function bindEvents() {
    const createBtn = qs('#submit');
    if(createBtn) createBtn.addEventListener('click', onCreateShiftPattern);
    const cancelBtn = qs('#cancel');
    if(cancelBtn) cancelBtn.addEventListener('click', onCancelShiftPattern);
}

(function init() {
  bindEvents();
})();