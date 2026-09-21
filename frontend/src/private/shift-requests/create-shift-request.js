import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs = (s, root = document) => root.querySelector(s);
const qsAll = (s, root = document) => Array.from((root || document).querySelectorAll(s));

const alertWarning = qs('.alert-warning'); 
const alertError = qs('.alert-error');
const alertSuccess = qs('.alert-success');

const DAY_INDEX = {
  "Lunes": 0, "Martes": 1, "Miércoles": 2, "Jueves": 3,
  "Viernes": 4, "Sábado": 5, "Domingo": 6
};
const DAYS = Object.keys(DAY_INDEX);

/* --- Util: reindexa los name="schedules[N][...]" después de eliminaciones --- */
function reindexBlocks(container = document.querySelector('#shiftDayRanges')) {
  const blocks = qsAll('.day-range-block', container);
  blocks.forEach((block, idx) => {
    qsAll('input, select, textarea', block).forEach(inp => {
      const name = inp.getAttribute('name');
      if (!name) return;
      const newName = name.replace(/schedules\[\d+\]/, `schedules[${idx}]`);
      inp.setAttribute('name', newName);
    });
  });
}

/* --- Validación de solapamiento de tramos (sin cambios) --- */
function validateNoOverlap(schedules) {
  const ranges = schedules.map(s => {
    let from = DAY_INDEX[s.dayFrom];
    let to = DAY_INDEX[s.dayTo];
    if (from > to) to += 7;
    return { from, to, orig: s };
  });
  for (let i = 0; i < ranges.length; i++) {
    for (let j = i + 1; j < ranges.length; j++) {
      const a = ranges[i], b = ranges[j];
      for (let ai = a.from; ai <= a.to; ai++) {
        for (let bi = b.from; bi <= b.to; bi++) {
          if ((ai % 7) === (bi % 7)) return [a.orig, b.orig];
        }
      }
    }
  }
  return null;
}

async function createShiftRequest() {
  const createBtn = qs('#submit');
  const cancelBtn = qs('#cancel');
  const siteIdRaw = qs('#shiftRequestSite')?.value;
  const accountIdRaw = qs('#shiftRequestAccount')?.value;
  const type = qs('#shiftRequestServiceType')?.value;
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value || null;
  const description = qs('#shiftRequestDescription')?.value?.trim() || null;
  const siteId = siteIdRaw ? parseInt(siteIdRaw, 10) : null;
  const accountId = accountIdRaw ? parseInt(accountIdRaw, 10) : null;
  if (!siteId) {
    displayAlert(alertError, 'Debe seleccionar un sitio.');
    return;
  }
  if (!type) {
    displayAlert(alertError, 'Debe seleccionar el tipo de servicio.');
    return;
  }
  if (!startDate) {
    displayAlert(alertError, 'La fecha de inicio es obligatoria.');
    return;
  }
  // --- Obtención de tramos ---
  const schedules = [];
  qsAll('.day-range-block').forEach((block, idx) => {
    const from = block.querySelector('.dayFrom')?.value;
    const to = block.querySelector('.dayTo')?.value;
    const startTime = block.querySelector('input[name$="[startTime]"]')?.value;
    const endTime = block.querySelector('input[name$="[endTime]"]')?.value;
    if (from && to && startTime && endTime)
      schedules.push({ dayFrom: from, dayTo: to, startTime, endTime });
  });
  if (schedules.length === 0) {
    displayAlert(alertError, 'Debe ingresar al menos un tramo de horario.');
    return;
  }
  // --- Validación de solapamiento ---
  const overlap = validateNoOverlap(schedules);
  if (overlap) {
    displayAlert(alertError, `Solapamiento de días entre "${overlap[0].dayFrom}
                              a ${overlap[0].dayTo}" y "${overlap[1].dayFrom}
                              a ${overlap[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);
    return;
  }
  if (createBtn) createBtn.disabled = true;
  if (cancelBtn) cancelBtn.disabled = true;
  try {
    const res = await fetchWithAuth('/api/shift-requests/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        siteId, type, clientAccountId: accountId, startDate, endDate, description, schedules
      })
    });
    if (!res || !res.ok) {
      let errorMessage = 'Ocurrió un problema al enviar el formulario.';
      if(res){
        const contentType = res.headers.get('content-type');
        if(contentType && contentType.includes('application/json')) {
          const errorData = await res.json();
          errorMessage = errorData.message || errorMessage;
        }
      }
      displayAlert(alertError, `Error: ${errorMessage}`);
      if(createBtn) createBtn.disabled = false;
      if(cancelBtn) cancelBtn.disabled = false;
      return;
    }
    displayAlert(alertSuccess, 'La asignación de turno ha sido creada correctamente.', 2000);
    setTimeout(() => {
          navigateTo('/private/shift-assignments/list', true); }, 2000);
  } catch (error) {
    console.error(`[onClickCreate] Ocurrio un problema: ${error.message}`, error);
    displayAlert(alertError, 'Error inesperado. Intente más tarde.', 2000);
    if(createBtn) createBtn.disabled = false;
    if(cancelBtn) cancelBtn.disabled = false;
  }
}

/* --- Añade un bloque (plantilla ahora usa input-with-icon para time fields) --- */
function addDayRangeBlock(prefill) {
  const shiftDayRanges = qs('#shiftDayRanges');
  const idx = qsAll('.day-range-block').length;
  const daysOptions = DAYS.map(d =>
    `<option value="${d}"${prefill && prefill.dayFrom === d ? ' selected' : ''}>${d}</option>` ).join('');
  const daysOptionsTo = DAYS.map(d =>
    `<option value="${d}"${prefill && prefill.dayTo === d ? ' selected' : ''}>${d}</option>` ).join('');
  const block = document.createElement('div');
  // leave only 'day-range-block' so its own CSS grid applies cleanly
  block.className = 'day-range-block';
  block.innerHTML = `
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${idx}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${daysOptions}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${idx}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${daysOptionsTo}
      </select>
    </div>

    <div class="form-group">
      <label>Hora inicio</label>
      <div class="time-field">
        <input type="time" name="schedules[${idx}][startTime]" value="${(prefill && prefill.startTime)||''}" required />
        <span class="time-icon"></span>
      </div>
    </div>

    <div class="form-group">
      <label>Hora fin</label>
      <div class="time-field">
        <input type="time" name="schedules[${idx}][endTime]" value="${(prefill && prefill.endTime)||''}" required />
        <span class="time-icon"></span>
      </div>
    </div>
    <div class="form-group">
      <button type="button" class="remove-schedule-btn">Quitar</button>
    </div>
  `;
  // bind remove + reindex
  block.querySelector('.remove-schedule-btn').addEventListener('click', () => {
    block.remove();
    reindexBlocks(shiftDayRanges);
    // if none left, ensure at least one block remains
    if (!shiftDayRanges.querySelector('.day-range-block')) addDayRangeBlock();
  });
  shiftDayRanges.appendChild(block);
  reindexBlocks(shiftDayRanges);
}

const cancelShiftRequest = () => {
    displayAlert(alertWarning,
                'La solicitud de turno ha sido cancelada', 1500);
    setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1500);
}

function bindEvents () {
  const createBtn = qs('#submit');
  if (createBtn) {
    createBtn.addEventListener('click', createShiftRequest);
  }
  const cancelBtn = qs('#cancel');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', cancelShiftRequest);
  }
  const shiftDayRanges = qs('#shiftDayRanges');
  const addDayRangeBtn = qs('#addDayRange');
  if (shiftDayRanges && addDayRangeBtn) {
    addDayRangeBtn.addEventListener('click', () => addDayRangeBlock());
    if (!shiftDayRanges.querySelector('.day-range-block')) addDayRangeBlock();
  }
  const projectSelect = qs('#projectExtenalId');
  if (projectSelect) {
    projectSelect.addEventListener('change', handleProjectChange);
  }
}

async function handleProjectChange () {
  const projectExternalId = qs('#projectExternalId')?.value;
  const siteSelect = qs('#shiftRequestSite');
  if(!projectExternalId) return;
  
  const urlSites = `/api/sites/projects/${projectExternalId}/sites`;
  const res = fetchWithAuth(urlSites, {
                        method: GET,
                        headers: {
                        'Accept': 'application/json'
                        }
  });
  if(!res) throw new Error('No se pudieron obtener los sitios del proyecto seleccionado.');
  const sitesProject = await res.json();
  if(sitesProject.length === 0){
    siteSelect.innerHTML = '<option value="">No hay sitios disponibles para el proyecto seleccionado</option>';
  } else {
    siteSelect.innerHTML = '<option value="">Seleccione un sitio</option>';
    sitesProject.forEach(site => {
      const option = document.createElement('option');
      option.value = site.externalId;
      option.textContent = site.name;
      siteSelect.appendChild(option);
    });
    siteSelect.disabled = false;
  }

}
// --- Cargar ClientAccounts al cambiar de Site ---
const _accountsCache = new Map(); // siteId -> accounts array

async function populateAccountsSelect(selectEl, accounts, preserveValue) {
  selectEl.innerHTML = '<option value="">Seleccione cuenta</option>';
  if (!accounts || accounts.length === 0) {
    selectEl.innerHTML = '<option value="">No hay cuentas asociadas</option>';
    selectEl.disabled = true;
    return;
  }
  accounts.forEach(a => {
    const opt = document.createElement('option');
    opt.value = a.id;
    opt.textContent = a.name;
    selectEl.appendChild(opt);
  });
  // si el valor previo sigue disponible, restaurarlo; si no, deja en default
  if (preserveValue) {
    const stillThere = Array.from(selectEl.options).some(o => o.value === String(preserveValue));
    if (stillThere) selectEl.value = String(preserveValue);
  }
  selectEl.disabled = false;
}

async function loadAccountsForSite(siteId) {
  const accountSelect = qs('#shiftRequestAccount');
  if (!accountSelect) return;
  // preserve current selection (if any)
  const prev = accountSelect.value || '';
  // empty / loading state
  accountSelect.disabled = true;
  accountSelect.innerHTML = '<option value="">Cargando cuentas...</option>';
  if (!siteId) {
    accountSelect.innerHTML = '<option value="">Seleccione cuenta</option>';
    accountSelect.disabled = true;
    return;
  }
  // cache hit
  if (_accountsCache.has(siteId)) {
    populateAccountsSelect(accountSelect, _accountsCache.get(siteId), prev);
    return;
  }
  try {
    // Ajusta la URL si tu endpoint es distinto (p.ej. /api/sites/{siteId}/accounts)
    const url = `/api/shift-requests/sites/${siteId}/accounts`;
    const resp = await fetchWithAuth(url, { method: 'GET' });
    if (resp.status === 401) {
      accountSelect.innerHTML = '<option value="">No autenticado</option>';
      return;
    }
    if (resp.status === 403) {
      accountSelect.innerHTML = '<option value="">Sin acceso a las cuentas</option>';
      return;
    }
    if (!resp.ok) {
      accountSelect.innerHTML = '<option value="">Error cargando cuentas</option>';
      return;
    }
    const accounts = await resp.json(); // [{id,name,...}, ...]
    // cachear (si quieres invalidar al crear cuentas, limpia _accountsCache)
    _accountsCache.set(siteId, accounts);
    populateAccountsSelect(accountSelect, accounts, prev);
  } catch (err) {
    console.error('Error cargando accounts:', err);
    accountSelect.innerHTML = '<option value="">Error cargando cuentas</option>';
    accountSelect.disabled = true;
  }
}

function bindSiteChangeLoader() {
  const siteSelect = qs('#shiftRequestSite');
  if (!siteSelect) return;

  siteSelect.addEventListener('change', (e) => {
    const siteId = e.target.value || null;
    // si quieres debounce, añádelo aquí
    loadAccountsForSite(siteId);
  });

  // si hay site ya seleccionado (edición), cargar al inicializar
  if (siteSelect.value) loadAccountsForSite(siteSelect.value);
}

(function init() {
  bindEvents();
  bindSiteChangeLoader();
})();