import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

const MAX_FILE_BYTES = 5 * 1024 * 1024; // 5 MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

function clearPreviewAndRevoke(previewEl) {
  if (!previewEl) return;
  // revoke any object URLs we stored on img[data-preview-url]
  const imgs = previewEl.querySelectorAll('img[data-preview-url]');
  imgs.forEach(img => {
    const url = img.getAttribute('data-preview-url');
    if (url) URL.revokeObjectURL(url);
  });
  previewEl.innerHTML = '';
}

function renderImagePreview(file, previewEl) {
  clearPreviewAndRevoke(previewEl);
  const url = URL.createObjectURL(file);
  const img = document.createElement('img');
  img.className = 'preview-img';
  img.src = url;
  img.setAttribute('data-preview-url', url);
  previewEl.appendChild(img);

  const actions = document.createElement('div');
  actions.className = 'preview-actions';

  const btnRemove = document.createElement('button');
  btnRemove.type = 'button';
  btnRemove.className = 'btn-remove';
  btnRemove.textContent = 'Quitar';
  btnRemove.addEventListener('click', () => {
    // limpiar input file
    const inputEl = qs('#incidentPhoto');
    if (inputEl) inputEl.value = '';
    clearPreviewAndRevoke(previewEl);
  });

  actions.appendChild(btnRemove);
  previewEl.appendChild(actions);
}

/**
 * (Opcional) Compresión/redimensionado en cliente.
 * Mantengo la función como helper por si quieres activarla.
 * Devuelve una Promise<Blob>.
 */
async function resizeImageFile(file, maxWidth = 1600, maxHeight = 1200, quality = 0.85) {
  if (!file.type.startsWith('image/')) return file;
  // Si no soporta canvas, devolver original
  if (!HTMLCanvasElement.prototype.toBlob) return file;

  return new Promise((resolve) => {
    const img = new Image();
    const url = URL.createObjectURL(file);
    img.onload = () => {
      let w = img.naturalWidth;
      let h = img.naturalHeight;
      let ratio = Math.min(1, maxWidth / w, maxHeight / h);
      if (ratio >= 1) {
        URL.revokeObjectURL(url);
        resolve(file);
        return;
      }
      const cw = Math.round(w * ratio);
      const ch = Math.round(h * ratio);
      const canvas = document.createElement('canvas');
      canvas.width = cw;
      canvas.height = ch;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, cw, ch);
      canvas.toBlob((blob) => {
        URL.revokeObjectURL(url);
        if (blob && blob.size > 0) {
          // asignar tipo igual al original si posible
          const out = new File([blob], file.name, { type: blob.type || file.type });
          resolve(out);
        } else {
          resolve(file);
        }
      }, 'image/jpeg', quality);
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      resolve(file);
    };
    img.src = url;
  });
}

async function onSubmitIncident(e) {
  e.preventDefault();
  const err = qs('#createIncidentError');
  const ok  = qs('#createIncidentOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  const siteId       = qs('#incidentSite')?.value || '';
  const incidentType = qs('#incidentType')?.value || '';
  const description  = qs('#incidentDescription')?.value?.trim() || '';
  const priority     = qs('#incidentPriority')?.value || '';
  const photoInput   = qs('#incidentPhoto');
  const file         = photoInput?.files?.[0] || null;

  if (!siteId) { if (err) err.textContent = 'Debes seleccionar un sitio.'; return; }
  if (!incidentType) { if (err) err.textContent = 'Debes seleccionar un tipo de incidente.'; return; }
  if (!priority) { if (err) err.textContent = 'Debes seleccionar una prioridad.'; return; }

  // Validación de archivo (si existe)
  if (file) {
    if (!ALLOWED_TYPES.includes(file.type)) {
      if (err) err.textContent = 'Formato de archivo no permitido. Usa JPG/PNG/WebP.';
      return;
    }
    if (file.size > MAX_FILE_BYTES) {
      if (err) err.textContent = 'El archivo excede el tamaño máximo de 5 MB.';
      return;
    }
  }

  // Deshabilitar submit mientras se procesa
  const submitBtn = qs('#createIncidentForm button[type="submit"]');
  if (submitBtn) submitBtn.disabled = true;

  try {
    const formData = new FormData();
    formData.append('siteId', siteId);
    formData.append('incidentType', incidentType);
    formData.append('priority', priority);
    formData.append('description', description);

    if (file) {
      // Opción: comprimir/redimensionar antes de enviar -> descomentar si quieres usar
      // const toUpload = await resizeImageFile(file, 1600, 1200, 0.8);
      const toUpload = file;
      formData.append('photo', toUpload); // el backend debe recibir MultipartFile photo
    }

    const res = await fetchWithAuth('/api/incidents/create', {
      method: 'POST',
      body: formData
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || 'No se pudo crear el incidente');
    }

    if (ok) {
      ok.textContent = 'Incidente creado ✅';
      ok.style.display = 'block';
    }

    // opcional: limpiar form y preview
    qs('#createIncidentForm')?.reset();
    clearPreviewAndRevoke(qs('#incidentPhotoPreview'));

    setTimeout(() => navigateTo('/private/incidents/table-view'), 600);
  } catch (error) {
    if (err) err.textContent = error.message || 'Error al crear incidente';
  } finally {
    if (submitBtn) submitBtn.disabled = false;
  }
}

function bindPhotoPreview() {
  const inp = qs('#incidentPhoto');
  const preview = qs('#incidentPhotoPreview');
  if (!inp || !preview) return;
  inp.addEventListener('change', () => {
    clearPreviewAndRevoke(preview);
    const f = inp.files?.[0];
    if (!f) return;
    if (f.type.startsWith('image/')) {
      renderImagePreview(f, preview);
    } else {
      const p = document.createElement('div');
      p.textContent = f.name;
      preview.appendChild(p);
    }
  });
}

function onCancelIncident(e) {
  e.preventDefault();
  // limpiar preview y form
  qs('#createIncidentForm')?.reset();
  clearPreviewAndRevoke(qs('#incidentPhotoPreview'));
  navigateTo('/private/incidents/table-view');
}

function bindIncidentForm() {
  qs('#createIncidentForm')?.addEventListener('submit', onSubmitIncident);
  qs('#cancelCreateIncident')?.addEventListener('click', onCancelIncident);
  bindPhotoPreview();
}

export function initCreateIncident() {
  bindIncidentForm();
  console.log('create-incident: listeners ligados');
}

// Auto-init fallback
if (document.readyState === 'complete' || document.readyState === 'interactive') {
  setTimeout(() => { if (qs('#createIncidentForm')) bindIncidentForm(); }, 50);
} else {
  document.addEventListener('DOMContentLoaded', () => setTimeout(() => { if (qs('#createIncidentForm')) bindIncidentForm(); }, 50));
}