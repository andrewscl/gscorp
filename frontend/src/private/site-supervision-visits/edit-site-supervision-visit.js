import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

// Guardar cambios en visita de supervisión
const form = document.getElementById('editSiteSupervisionVisitForm');
if (form) {
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();

    // Se asume que el ID está en un campo oculto o en el modelo visit.id
    const id = form.querySelector('input[name="id"]')?.value 
      || document.querySelector('.meta-id span')?.textContent?.trim();
    if (!id) {
      alert("No se pudo determinar el ID de la visita.");
      return;
    }

    const fd = new FormData(form);

    const errorDiv = document.getElementById('editVisitError');
    const okDiv = document.getElementById('editVisitOk');
    errorDiv.textContent = '';
    okDiv.style.display = 'none';

    try {
      const res = await fetchWithAuth(`/api/site-supervision-visits/update/${id}`, {
        method: 'POST',
        body: fd,
        credentials: 'same-origin'
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `No se pudo guardar (HTTP ${res.status})`);
      }

      okDiv.style.display = '';
      setTimeout(() => navigateTo('/private/site-supervision-visits/table-view', true), 1300);
    } catch (e) {
      errorDiv.textContent = e.message || 'Error al guardar los cambios.';
    }
  });
}

// Geolocalización: botón para obtener lat/lon y completar campos en el formulario
const getLocationBtn = document.getElementById('getLocationBtn');
const latInput = document.getElementById('visitLat');
const lonInput = document.getElementById('visitLon');

if (getLocationBtn && latInput && lonInput) {
  getLocationBtn.addEventListener('click', async () => {
    if (!navigator.geolocation) {
      alert("Geolocalización no soportada por tu navegador.");
      return;
    }

    getLocationBtn.disabled = true;
    getLocationBtn.textContent = "Obteniendo ubicación...";

    navigator.geolocation.getCurrentPosition(
      (position) => {
        latInput.value = position.coords.latitude;
        lonInput.value = position.coords.longitude;
        getLocationBtn.textContent = "Ubicación obtenida ✅";
        setTimeout(() => {
          getLocationBtn.textContent = "Obtener ubicación actual";
          getLocationBtn.disabled = false;
        }, 1500);
        alert(`Precisión estimada: ${Math.round(position.coords.accuracy)} metros`);
      },
      (error) => {
        alert("No se pudo obtener la ubicación: " + error.message);
        getLocationBtn.textContent = "Obtener ubicación actual";
        getLocationBtn.disabled = false;
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  });
}

// Previsualización de foto y video
const photoInput = document.getElementById('photoInput');
const videoInput = document.getElementById('videoInput');
const photoPreviewDiv = document.getElementById('photoPreview');
const videoPreviewDiv = document.getElementById('videoPreview');

if (photoInput && photoPreviewDiv) {
  photoInput.addEventListener('change', () => {
    photoPreviewDiv.innerHTML = '';
    const file = photoInput.files && photoInput.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      const img = document.createElement('img');
      img.src = url;
      img.style.maxWidth = "180px";
      img.style.borderRadius = "6px";
      photoPreviewDiv.appendChild(img);
    }
  });
}
if (videoInput && videoPreviewDiv) {
  videoInput.addEventListener('change', () => {
    videoPreviewDiv.innerHTML = '';
    const file = videoInput.files && videoInput.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      const video = document.createElement('video');
      video.src = url;
      video.controls = true;
      video.style.maxWidth = "220px";
      video.style.borderRadius = "6px";
      videoPreviewDiv.appendChild(video);
    }
  });
}