import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-incident.js cargado');

const qs = (s) => document.querySelector(s);

async function onSubmitIncident(e) {
  e.preventDefault();
  
  // Campos
  const siteId       = qs('#incidentSite')?.value || '';
  const typeId       = qs('#incidentType')?.value || '';
  const description  = qs('#incidentDescription')?.value?.trim() || '';
  const slaMinutes   = qs('#incidentSlaMinutes')?.value || '30';
  
  const err = qs('#createIncidentError');
  const ok  = qs('#createIncidentOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  // Validación básica
  if (!siteId) {
    if (err) err.textContent = 'Debes seleccionar un sitio.';
    return;
  }
  if (!typeId) {
    if (err) err.textContent = 'Debes seleccionar un tipo de incidente.';
    return;
  }

  try {
    // Construye FormData
    const formData = new FormData();
    formData.append('siteId', siteId);
    formData.append('typeId', typeId);
    formData.append('description', description);
    formData.append('slaMinutes', slaMinutes);

    const res = await fetchWithAuth('/api/incidents/create', {
      method: 'POST',
      body: formData
    });

    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      throw new Error(msg || 'No se pudo crear el incidente');
    }

    if (ok) ok.style.display = 'block';
    ok.textContent = 'Incidente creado ✅';
    setTimeout(() => {
      navigateTo('/private/incidents/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  }
}

function onCancelIncident(e) {
  e.preventDefault();
  navigateTo('/private/incidents/table-view');
}

function bindIncidentForm() {
  qs('#createIncidentForm')?.addEventListener('submit', onSubmitIncident);
  qs('#cancelCreateIncident')?.addEventListener('click', onCancelIncident);
  // Si tienes un botón para volver ("Volver a la lista")
  qs('#goToIncidentTable')?.addEventListener('click', onCancelIncident);
}

(function init() {
  bindIncidentForm();
})();