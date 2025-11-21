import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-forecast.js cargado');

const qs = (s, ctx = document) => ctx.querySelector(s);

// Mensajes en #forecast-status
function showStatus(msg, { error = false, timeout = 4000 } = {}) {
  const el = qs('#forecast-status');
  if (!el) return;
  el.textContent = msg;
  el.style.color = error ? '#b91c1c' : '';
  if (timeout > 0) {
    setTimeout(() => { if (el) el.style.color = ''; }, timeout);
  }
}

// Limpia preview
function clearPreview() {
  const p = qs('#forecast-preview');
  const t = qs('#forecast-preview-text');
  if (!p || !t) return;
  t.textContent = '';
  p.hidden = true;
}

// Construye payload alineado con ForecastCreateDto
function buildPayload(form) {
  const clientId = qs('#forecast-client', form)?.value ?? '';
  const projectId = qs('#forecast-project', form)?.value ?? '';
  const siteId = qs('#forecast-site', form)?.value ?? '';
  const metric = qs('#forecast-metric', form)?.value ?? '';
  const periodicity = qs('#forecast-periodicity', form)?.value ?? '';
  const forecastCategory = qs('#forecast-category', form)?.value ?? '';
  const periodStart = qs('#forecast-start', form)?.value ?? '';
  const periodEnd = qs('#forecast-end', form)?.value ?? '';
  const periodStartHour = (() => {
    const el = qs('#forecast-start-hour', form);
    if (!el) return null;
    const v = String(el.value ?? '').trim();
    return v === '' ? null : Number(v);
  })();
  const periodEndHour = (() => {
    const el = qs('#forecast-end-hour', form);
    if (!el) return null;
    const v = String(el.value ?? '').trim();
    return v === '' ? null : Number(v);
  })();
  const valueRaw = qs('#forecast-value', form)?.value ?? '';
  const value = valueRaw === '' ? null : Number(valueRaw);
  const units = qs('#forecast-units', form)?.value ?? null;
  const confidenceRaw = qs('#forecast-confidence', form)?.value ?? '';
  const confidence = confidenceRaw === '' ? null : Number(confidenceRaw);
  const note = qs('#forecast-note', form)?.value ?? null;
  const versionRaw = qs('#forecast-version', form)?.value ?? '';
  const forecastVersion = versionRaw === '' ? null : Number(versionRaw);
  const tz = qs('#forecast-tz', form)?.value ?? null;

  return {
    clientId: clientId === '' ? null : Number(clientId),
    projectId: projectId === '' ? null : Number(projectId),
    siteId: siteId === '' ? null : Number(siteId),
    periodicity: periodicity || null,
    metric: metric || null,
    forecastCategory: forecastCategory || null,
    periodStart: periodStart || null,
    periodEnd: periodEnd || null,
    periodStartHour: periodStartHour,
    periodEndHour: periodEndHour,
    value: value,
    units: units || null,
    confidence: confidence,
    note: note || null,
    forecastVersion: forecastVersion,
    tz: tz || null
  };
}

function basicValidation(payload) {
  if (!payload.clientId) return 'Debes seleccionar un cliente.';
  if (!payload.metric) return 'La métrica es requerida.';
  if (!payload.periodicity) return 'La periodicidad es requerida.';
  if (!payload.forecastCategory) return 'La categoría es requerida.';
  if (!payload.periodStart) return 'El periodo inicio es requerido.';
  if (payload.value == null || Number.isNaN(payload.value)) return 'Valor válido es requerido.';
  if (payload.value < 0) return 'El valor debe ser >= 0.';
  if (payload.confidence != null && (Number.isNaN(payload.confidence) || payload.confidence < 0 || payload.confidence > 100)) {
    return 'Confianza debe estar entre 0 y 100.';
  }
  if (payload.periodicity === 'HOURLY') {
    if (payload.periodStartHour == null || payload.periodEndHour == null) return 'Para HOURLY especifica las horas.';
    if (!Number.isInteger(payload.periodStartHour) || payload.periodStartHour < 0 || payload.periodStartHour > 23) return 'periodStartHour debe ser 0..23';
    if (!Number.isInteger(payload.periodEndHour) || payload.periodEndHour < 0 || payload.periodEndHour > 23) return 'periodEndHour debe ser 0..23';
  }
  if (payload.periodEnd) {
    try {
      const s = new Date(payload.periodStart);
      const e = new Date(payload.periodEnd);
      if (s > e) return "'Desde' no puede ser posterior a 'Hasta'.";
    } catch (e) {
      // ignore
    }
  }
  return null;
}

async function submitForecast(endpoint, payload) {
  const res = await fetchWithAuth(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  return res;
}

/* --- Handlers --- */
async function onSubmitCreate(e) {
  e.preventDefault();
  const form = qs('#forecast-form');
  if (!form) return;

  const errEl = qs('#forecast-status');
  if (errEl) errEl.textContent = '';

  const submitBtn = qs('#forecast-save', form);
  if (submitBtn) submitBtn.disabled = true;

  try {
    const payload = buildPayload(form);
    const vErr = basicValidation(payload);
    if (vErr) {
      showStatus(vErr, { error: true, timeout: 5000 });
      if (submitBtn) submitBtn.disabled = false;
      return;
    }

    // preview
    const previewEl = qs('#forecast-preview');
    if (previewEl) {
      const previewTextEl = qs('#forecast-preview-text');
      if (previewTextEl) previewTextEl.textContent = [
        payload.clientId ? `Cliente: ${payload.clientId}` : null,
        payload.projectId ? `Proyecto: ${payload.projectId}` : null,
        payload.siteId ? `Sitio: ${payload.siteId}` : null,
        payload.metric ? `Métrica: ${payload.metric}` : null,
        payload.periodicity ? `Periodicidad: ${payload.periodicity}` : null,
        payload.periodStart ? `Desde: ${payload.periodStart}` : null,
        payload.periodEnd ? `Hasta: ${payload.periodEnd}` : null,
        payload.value != null ? `Valor: ${payload.value}${payload.units ? ' ' + payload.units : ''}` : null,
        payload.tz ? `Zona: ${payload.tz}` : null
      ].filter(Boolean).join(' · ');
      previewEl.hidden = false;
    }

    showStatus('Enviando...', { error: false, timeout: 0 });

    const endpoint = form.dataset.forecastEndpoint || form.getAttribute('data-forecast-endpoint') || '/api/forecasts';
    const res = await submitForecast(endpoint, payload);
    if (!res.ok) {
      let msg = `Error ${res.status}`;
      try {
        const body = await res.json();
        msg = body?.message || body?.error || JSON.stringify(body);
      } catch (e) {
        msg = await res.text().catch(() => msg);
      }
      throw new Error(msg);
    }

    showStatus('Forecast creado correctamente', { error: false, timeout: 1500 });
    form.reset();
    clearPreview();

    // redirigir
    setTimeout(() => {
      const redirect = form.dataset.postUrl || '/private/forecast/table-view';
      navigateTo(redirect, true);
    }, 600);
  } catch (err) {
    showStatus('No se pudo crear: ' + (err.message || err), { error: true, timeout: 6000 });
  } finally {
    if (submitBtn) submitBtn.disabled = false;
  }
}

function onCancel(e) {
  e.preventDefault();
  const form = qs('#forecast-form');
  if (!form) return;
  const cancelPath = form.dataset.cancelPath || '/private/forecast';
  navigateTo(cancelPath, true);
}

/* --- Helpers: TZ detect, periodicity toggle, cascading selects --- */
function initDetectTz() {
  const btn = qs('#detect-tz');
  const tzInput = qs('#forecast-tz');
  if (!btn || !tzInput) return;
  btn.addEventListener('click', () => {
    try {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
      if (tz) {
        tzInput.value = tz;
        showStatus(`Zona detectada: ${tz}`, { error: false, timeout: 2000 });
      } else {
        showStatus('No se pudo detectar la zona', { error: true, timeout: 2000 });
      }
    } catch (e) {
      console.warn('Detect TZ failed', e);
      showStatus('Error detectando zona', { error: true, timeout: 2000 });
    }
  });
}

function toggleHourlyFields() {
  const sel = qs('#forecast-periodicity');
  const hourly = qs('.hour-fields');
  if (!sel || !hourly) return;
  if (sel.value === 'HOURLY') {
    hourly.hidden = false;
    hourly.setAttribute('aria-hidden', 'false');
  } else {
    hourly.hidden = true;
    hourly.setAttribute('aria-hidden', 'true');
    const s = qs('#forecast-start-hour');
    const e = qs('#forecast-end-hour');
    if (s) s.value = '';
    if (e) e.value = '';
  }
}

function initPeriodicityToggle() {
  const sel = qs('#forecast-periodicity');
  if (!sel) return;
  sel.addEventListener('change', toggleHourlyFields);
  toggleHourlyFields();
}

async function fetchAndPopulateProjects(clientId) {
  const projectSel = qs('#forecast-project');
  const siteSel = qs('#forecast-site');
  if (!projectSel) return;
  projectSel.innerHTML = '<option value="">-- seleccionar --</option>';
  if (siteSel) {
    siteSel.innerHTML = '<option value="">-- (opcional) --</option>';
    siteSel.disabled = true;
  }
  if (!clientId) { projectSel.disabled = true; return; }

  try {
    const res = await fetchWithAuth(`/api/clients/${clientId}/projects`);
    if (!res.ok) throw new Error('no projects');
    const projects = await res.json();
    projects.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = p.name || p.id;
      projectSel.appendChild(opt);
    });
    projectSel.disabled = false;
  } catch (e) {
    console.debug('No projects endpoint or failed to load projects', e);
    projectSel.disabled = true;
  }
}

async function fetchAndPopulateSites(projectId) {
  const siteSel = qs('#forecast-site');
  if (!siteSel) return;
  siteSel.innerHTML = '<option value="">-- (opcional) --</option>';
  if (!projectId) { siteSel.disabled = true; return; }

  try {
    const res = await fetchWithAuth(`/api/projects/${projectId}/sites`);
    if (!res.ok) throw new Error('no sites');
    const sites = await res.json();
    sites.forEach(s => {
      const opt = document.createElement('option');
      opt.value = s.id;
      opt.textContent = s.name || s.id;
      siteSel.appendChild(opt);
    });
    siteSel.disabled = false;
  } catch (e) {
    console.debug('No sites endpoint or failed to load sites', e);
    siteSel.disabled = true;
  }
}

function initCascadingSelects() {
  const clientSel = qs('#forecast-client');
  const projectSel = qs('#forecast-project');

  if (clientSel) {
    clientSel.addEventListener('change', async (ev) => {
      const clientId = ev.target.value;
      await fetchAndPopulateProjects(clientId);
      clearPreview();
    });
  }

  if (projectSel) {
    projectSel.addEventListener('change', async (ev) => {
      const projectId = ev.target.value;
      await fetchAndPopulateSites(projectId);
      clearPreview();
    });
  }
}

/* --- Bindings --- */
function bindCreateForm() {
  const form = qs('#forecast-form');
  if (!form) return;
  form.addEventListener('submit', onSubmitCreate);

  const cancelBtn = qs('#forecast-cancel');
  if (cancelBtn) cancelBtn.addEventListener('click', onCancel);

  initDetectTz();
  initPeriodicityToggle();
  initCascadingSelects();
}

/* --- Init --- */
(function init() {
  bindCreateForm();
})();