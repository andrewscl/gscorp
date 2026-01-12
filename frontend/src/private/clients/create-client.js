import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-client.js cargado');

// Selector reducido para simplificar el acceso al DOM
const qs = (s) => document.querySelector(s);

/* --- Limpia Mensajes --- */
function clearMessages() {
  const message = qs('#clientFormMessage');
  if (message) {
    message.textContent = '';
    message.classList.remove('error', 'success');
  }
}

/* --- Muestra Mensajes --- */
function showMessage(type, text) {
  const message = qs('#clientFormMessage');
  if (message) {
    message.textContent = text;
    message.classList.add(type); // `type` puede ser 'error' o 'success'
  }
}

/* --- Valida los Datos del Formulario --- */
function validateFormData({ name, contactEmail }) {
  if (!name) {
    showMessage('error', 'El nombre es obligatorio.');
    return false;
  }

  if (contactEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contactEmail)) {
    showMessage('error', 'El correo electrónico no es válido.');
    return false;
  }

  return true;
}

/* --- Enviar Formulario --- */
async function onSubmitCreate(e) {
  e.preventDefault();

  const name = qs('#clientName')?.value?.trim();
  const legalName = qs('#clientLegalName')?.value?.trim() || null;
  const taxId = qs('#clientTaxId')?.value?.trim() || null;
  const contactEmail = qs('#clientEmail')?.value?.trim() || null;
  const contactPhone = qs('#clientPhone')?.value?.trim() || null;
  const active = !!qs('#clientActive')?.checked;

  clearMessages();

  const formData = { name, legalName, taxId, contactEmail, contactPhone, active };

  if (!validateFormData(formData)) return;

  try {
    const response = await fetchWithAuth('/api/clients/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const error = await response.text().catch(() => 'Error desconocido');
      throw new Error(error || 'No se pudo crear el cliente.');
    }

    showMessage('success', 'Cliente creado correctamente.');

    setTimeout(() => {
      navigateTo('/private/clients/table-view');
    }, 1000);
  } catch (error) {
    showMessage('error', error.message || 'Hubo un error al crear el cliente.');
  }
}

/* --- Inicialización --- */
function bindCreateClientForm() {
  const form = qs('#createClientForm');
  if (form) form.addEventListener('submit', onSubmitCreate);
}

(function init() {
  bindCreateClientForm();
})();