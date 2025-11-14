import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

/**
 * edit-incident.js
 *
 * - Gestiona el envío del formulario de edición (multipart/form-data) usando fetchWithAuth.
 * - Maneja preview de la fotografía (crea/revoca objectURL).
 * - Soporta la opción "Eliminar foto existente" (checkbox).
 * - Usa navigateTo para volver al listado tras guardar.
 */

const form = document.getElementById('editIncidentForm');
const photoInput = document.getElementById('photoFile');
const photoPreviewImg = document.getElementById('photoPreviewImg');
const photoPreviewLink = document.getElementById('photoPreviewLink');
const photoPreviewContainer = document.getElementById('photoPreviewContainer');
const photoPreviewEmpty = document.getElementById('photoPreviewEmpty');
const removePhotoCheckbox = document.getElementById('removePhoto');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');
const errEl = document.getElementById('editIncidentError');
const okEl = document.getElementById('editIncidentOk');

let objectUrl = null;

/* util */
function clearPreviewAndRevoke() {
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
    objectUrl = null;
  }
}

/* Photo preview */
if (photoInput) {
  // preserve original src on the img for cancel/restore purposes
  if (photoPreviewImg && !photoPreviewImg.dataset.original) {
    photoPreviewImg.dataset.original = photoPreviewImg.src || '';
  }

  photoInput.addEventListener('change', (e) => {
    const file = e.target.files && e.target.files[0];
    // uncheck removePhoto when a new file is selected
    if (removePhotoCheckbox) removePhotoCheckbox.checked = false;

    if (!file) {
      // restore original preview if it exists
      clearPreviewAndRevoke();
      if (photoPreviewImg) {
        const orig = photoPreviewImg.dataset.original || '';
        if (orig) {
          photoPreviewImg.src = orig;
          if (photoPreviewContainer) photoPreviewContainer.style.display = '';
          if (photoPreviewEmpty) photoPreviewEmpty.style.display = 'none';
        } else {
          if (photoPreviewContainer) photoPreviewContainer.style.display = 'none';
          if (photoPreviewEmpty) photoPreviewEmpty.style.display = '';
        }
      }
      return;
    }

    // show new preview
    clearPreviewAndRevoke();
    objectUrl = URL.createObjectURL(file);

    if (photoPreviewImg) {
      photoPreviewImg.src = objectUrl;
      if (photoPreviewContainer) photoPreviewContainer.style.display = '';
      if (photoPreviewEmpty) photoPreviewEmpty.style.display = 'none';
      if (photoPreviewLink) photoPreviewLink.href = objectUrl;
    } else {
      // if no existing img element, create one inside the container
      const img = document.createElement('img');
      img.id = 'photoPreviewImg';
      img.className = 'incident-photo-thumb';
      img.src = objectUrl;
      img.alt = 'Foto incidente';
      if (photoPreviewContainer) {
        photoPreviewContainer.appendChild(img);
        photoPreviewContainer.style.display = '';
      } else if (photoPreviewEmpty) {
        photoPreviewEmpty.replaceWith(img);
      }
    }
  });
}

/* Remove photo checkbox toggles preview */
if (removePhotoCheckbox) {
  removePhotoCheckbox.addEventListener('change', () => {
    if (removePhotoCheckbox.checked) {
      // hide preview
      if (photoPreviewContainer) photoPreviewContainer.style.display = 'none';
      if (photoPreviewEmpty) photoPreviewEmpty.style.display = '';
      // clear file input
      if (photoInput) {
        photoInput.value = '';
        clearPreviewAndRevoke();
      }
    } else {
      // restore existing preview if any
      if (photoPreviewImg && (photoPreviewImg.dataset.original || photoPreviewImg.src)) {
        if (photoPreviewContainer) photoPreviewContainer.style.display = '';
        if (photoPreviewEmpty) photoPreviewEmpty.style.display = 'none';
      }
    }
  });
}

/* Cancel / volver */
if (cancelBtn) {
  cancelBtn.addEventListener('click', (ev) => {
    ev.preventDefault();
    const path = cancelBtn.getAttribute('data-path') || '/private/incidents/table-view';
    if (typeof navigateTo === 'function') {
      try {
        navigateTo(path);
      } catch (e) {
        window.location.href = path;
      }
    } else {
      window.location.href = path;
    }
  });
}

/* Submit handler */
if (form) {
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    if (!form) return;

    // clear UI messages
    if (errEl) errEl.textContent = '';
    if (okEl) okEl.style.display = 'none';
    if (saveBtn) saveBtn.disabled = true;

    try {
      // determine id from hidden input or meta-id span
      const id = form.querySelector('input[name="id"]')?.value
        || document.querySelector('.meta-id span')?.textContent?.trim();
      if (!id) throw new Error('ID del incidente no encontrado');

      const fd = new FormData();
      // append editable fields
      const typeEl = document.getElementById('incidentType');
      const priorityEl = document.getElementById('priority');
      const statusEl = document.getElementById('status');
      const slaEl = document.getElementById('slaMinutes');
      const descEl = document.getElementById('description');

      if (typeEl) fd.append('incidentType', typeEl.value);
      if (priorityEl) fd.append('priority', priorityEl.value);
      if (statusEl) fd.append('status', statusEl.value);
      if (slaEl) fd.append('slaMinutes', slaEl.value ?? '');
      if (descEl) fd.append('description', descEl.value ?? '');

      // file handling
      const file = photoInput?.files?.[0];
      if (file) {
        fd.append('photo', file);
      }

      // signal to remove existing photo
      if (removePhotoCheckbox && removePhotoCheckbox.checked) {
        fd.append('removePhoto', 'true');
      }

      // send request (POST to update endpoint)
      const url = `/api/incidents/${encodeURIComponent(id)}/update`;
      const res = await fetchWithAuth(url, {
        method: 'POST',
        body: fd,
        credentials: 'same-origin'
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo guardar (HTTP ${res.status})`);
      }

      if (okEl) {
        okEl.style.display = '';
        okEl.textContent = 'Cambios guardados ✅';
      }

      // cleanup preview objectURL if any
      clearPreviewAndRevoke();

      // small delay so user sees the success message
      setTimeout(() => {
        if (typeof navigateTo === 'function') navigateTo('/private/incidents/table-view', true);
        else window.location.href = '/private/incidents/table-view';
      }, 900);

    } catch (e) {
      if (errEl) errEl.textContent = e.message || 'Error al guardar los cambios.';
      console.error('Error al guardar incidente:', e);
    } finally {
      if (saveBtn) saveBtn.disabled = false;
    }
  });
}