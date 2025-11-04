import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);
const qsAll = (s) => Array.from(document.querySelectorAll(s));

const DAY_INDEX = {
  "Lunes": 0, "Martes": 1, "Miércoles": 2, "Jueves": 3,
  "Viernes": 4, "Sábado": 5, "Domingo": 6
};
const DAYS = Object.keys(DAY_INDEX);

/* --- Validación de solapamiento de tramos --- */
function validateNoOverlap(schedules) {
  // Convierte cada tramo a rango numérico
  const ranges = schedules.map(s => {
    let from = DAY_INDEX[s.dayFrom];
    let to = DAY_INDEX[s.dayTo];
    // Permitir tramos contiguos inversos (ej. Sábado a Lunes)
    if (from > to) to += 7;
    return { from, to, orig: s };
  });
  // Chequea solapamientos contra cada otro
  for (let i = 0; i < ranges.length; i++) {
    for (let j = i + 1; j < ranges.length; j++) {
      const a = ranges[i], b = ranges[j];
      // Normaliza ambos a posible semana extendida
      for (let ai = a.from; ai <= a.to; ai++) {
        for (let bi = b.from; bi <= b.to; bi++) {
          if ((ai % 7) === (bi % 7)) return [a.orig, b.orig]; // Hay solapamiento
        }
      }
    }
  }
  return null;
}

/* --- Crear solicitud de turno --- */
async function onSubmitCreateShiftRequest(e) {
  e.preventDefault();

  const code = qs('#shiftRequestCode')?.value?.trim();
  const siteId = qs('#shiftRequestSite')?.value;
  const accountId = qs('#shiftRequestAccount')?.value; // Nuevo campo Cuenta
  const serviceType = qs('#shiftRequestServiceType')?.value; // Nuevo campo Tipo de Servicio
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value || null;
  const status = qs('#shiftRequestStatus')?.value;
  const description = qs('#shiftRequestDescription')?.value?.trim() || null;

  const err = qs('#createShiftRequestError');
  const ok = qs('#createShiftRequestOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  // Validaciones mínimas
  if (!code)      { if (err) err.textContent = 'El código es obligatorio.'; return; }
  if (!siteId)    { if (err) err.textContent = 'Debe seleccionar un sitio.'; return; }
  if (!accountId) { if (err) err.textContent = 'Debe seleccionar una cuenta.'; return; }
  if (!serviceType) { if (err) err.textContent = 'Debe seleccionar el tipo de servicio.'; return; }
  if (!startDate) { if (err) err.textContent = 'La fecha de inicio es obligatoria.'; return; }
  if (!status)    { if (err) err.textContent = 'Debe seleccionar un estado.'; return; }

  // --- Obtención de tramos ---
  const schedules = [];
  qsAll('.day-range-block').forEach((block, idx) => {
    const from = block.querySelector('.dayFrom')?.value;
    const to = block.querySelector('.dayTo')?.value;
    const startTime = block.querySelector('input[name^="schedules"][name$="[startTime]"]')?.value;
    const endTime = block.querySelector('input[name^="schedules"][name$="[endTime]"]')?.value;
    const lunchTime = block.querySelector('input[name^="schedules"][name$="[lunchTime]"]')?.value || null;
    if (from && to && startTime && endTime)
      schedules.push({ dayFrom: from, dayTo: to, startTime, endTime, lunchTime });
  });
  if (schedules.length === 0) {
    if (err) err.textContent = 'Debe ingresar al menos un tramo de horario.';
    return;
  }
  // --- Validación de solapamiento ---
  const overlap = validateNoOverlap(schedules);
  if (overlap) {
    if (err) err.textContent = `Solapamiento de días entre "${overlap[0].dayFrom} a ${overlap[0].dayTo}" y "${overlap[1].dayFrom} a ${overlap[1].dayTo}". Ajuste los tramos para que no se crucen.`;
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createShiftRequestForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shift-requests/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code, siteId, accountId, serviceType, startDate, endDate, status, description, schedules
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }
    if (ok) ok.style.display = 'block';
    setTimeout(() => { navigateTo('/private/shift-requests/table-view'); }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Tramos de días y horarios dinámicos --- */
function addDayRangeBlock(prefill) {
  const shiftDayRanges = qs('#shiftDayRanges');
  const idx = qsAll('.day-range-block').length;
  const daysOptions = DAYS.map(d =>
    `<option value="${d}"${prefill && prefill.dayFrom === d ? ' selected' : ''}>${d}</option>` ).join('');
  const daysOptionsTo = DAYS.map(d =>
    `<option value="${d}"${prefill && prefill.dayTo === d ? ' selected' : ''}>${d}</option>` ).join('');
  const block = document.createElement('div');
  block.className = 'form-row day-range-block';
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
      <input type="time" name="schedules[${idx}][startTime]" value="${(prefill && prefill.startTime)||''}" required />
    </div>
    <div class="form-group">
      <label>Hora término</label>
      <input type="time" name="schedules[${idx}][endTime]" value="${(prefill && prefill.endTime)||''}" required />
    </div>
    <div class="form-group">
      <label>Colación</label>
      <input type="time" name="schedules[${idx}][lunchTime]" value="${(prefill && prefill.lunchTime)||''}" />
    </div>
    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `;
  block.querySelector('.remove-day-range').onclick = () => block.remove();
  shiftDayRanges.appendChild(block);
}

/* --- Bind para añadir tramos de horario --- */
function bindDayRangeAdder() {
  const shiftDayRanges = qs('#shiftDayRanges');
  const addDayRangeBtn = qs('#addDayRange');
  if (shiftDayRanges && addDayRangeBtn) {
    addDayRangeBtn.addEventListener('click', () => addDayRangeBlock());
    // Inicializa con un tramo por defecto si está vacío:
    if (!shiftDayRanges.querySelector('.day-range-block')) addDayRangeBlock();
  }
}

/* --- Bindings generales --- */
function bindCreateShiftRequestForm() {
  qs('#createShiftRequestForm')?.addEventListener('submit', onSubmitCreateShiftRequest);
}

function bindCancelCreateShiftRequest() {
  qs('#cancelCreateShiftRequest')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-requests/table-view');
  });
}

function bindCloseCreateShiftRequest() {
  qs('#closeCreateShiftRequest')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-requests/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateShiftRequestForm();
  bindCancelCreateShiftRequest();
  bindCloseCreateShiftRequest();
  bindDayRangeAdder();
})();