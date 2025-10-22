import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s) => document.querySelector(s);

/* --- Crear patrón de turno --- */
async function onSubmitCreateShiftPattern(e) {
  e.preventDefault();

  const name = qs('#shiftPatternName')?.value?.trim();
  const code = qs('#shiftPatternCode')?.value?.trim() || null;
  const description = qs('#shiftPatternDescription')?.value?.trim() || null;
  const workDaysStr = qs('#shiftPatternWorkDays')?.value?.trim();
  const restDaysStr = qs('#shiftPatternRestDays')?.value?.trim();
  const startDayStr = qs('#shiftPatternStartDay')?.value?.trim();
  const active = !!qs('#shiftPatternActive')?.checked;

  const workDays = workDaysStr ? Number(workDaysStr) : null;
  const restDays = restDaysStr ? Number(restDaysStr) : null;
  const startDay = startDayStr ? Number(startDayStr) : null;

  const err = qs('#createShiftPatternError');
  const ok = qs('#createShiftPatternOk');
  if (err) err.textContent = '';
  if (ok) ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre del patrón es obligatorio.';
    return;
  }
  if (!description) {
    if (err) err.textContent = 'La descripción es obligatoria.';
    return;
  }
  if (!workDays || isNaN(workDays) || workDays < 1) {
    if (err) err.textContent = 'Los días de trabajo son obligatorios y deben ser un número mayor a 0.';
    return;
  }
  if (restDays === null || isNaN(restDays) || restDays < 0) {
    if (err) err.textContent = 'Los días de descanso son obligatorios y deben ser 0 o más.';
    return;
  }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createShiftPatternForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shift-patterns/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        code,
        description,
        workDays,
        restDays,
        startDay,
        active
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      navigateTo('/private/shift-patterns/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

/* --- Bindings --- */
function bindCreateShiftPatternForm() {
  qs('#createShiftPatternForm')?.addEventListener('submit', onSubmitCreateShiftPattern);
}

function bindCancelCreateShiftPattern() {
  qs('#cancelCreateShiftPattern')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-patterns/table-view');
  });
}

function bindCloseCreateShiftPattern() {
  qs('#closeCreateShiftPattern')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/shift-patterns/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateShiftPatternForm();
  bindCancelCreateShiftPattern();
  bindCloseCreateShiftPattern();
})();