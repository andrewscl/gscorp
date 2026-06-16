import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { initTabPanels } from '../../shared/tabs-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

console.log('create-employee.js cargado');

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertCancel = qs('.alert-warning');

function onCancelCreate(e) {
    displayAlert(alertCancel, 'La creación del empleado ha sido cancelada.', 2000);
    navigateTo('/private/employees/table-view');
}

// El botón solo abre la ventana del sistema operativo
const onchangePhoto = () => {
    const photoInput = qs('#employeePhotoInput');
    if (photoInput) {
        photoInput.click();
    }
};

// Esta función procesa el archivo binario y actualiza la vista previa
const handlePhotoFileChange = function() {
    const photoPreview = qs('#employeePhotoPreview');
    const file = this.files[0]; // 'this' hace referencia al #employeePhotoInput

    if (file && photoPreview) {
        const reader = new FileReader();
        reader.onload = function(e) {
            photoPreview.src = e.target.result; // Cambia la foto en pantalla al instante
        };
        reader.readAsDataURL(file);
    }
};

function bindCreateEmployee() {
    const backBtn = qs('.btn-secondary');
    if (backBtn) {
        backBtn.addEventListener('click', onCancelCreate);
    }
    const changePhotoBtn = qs('#changePhotoBtn');
    if (changePhotoBtn) {
        changePhotoBtn.addEventListener('click', onchangePhoto);
    }
    // 3. Amarra de forma independiente el cambio de archivo al input oculto
    const photoInput = qs('#employeePhotoInput');
    if (photoInput) {
        photoInput.addEventListener('change', handlePhotoFileChange);
    }
}

(function init() {
    initTabPanels();
    bindCreateEmployee();
})();