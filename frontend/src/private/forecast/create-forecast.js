import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-forecast.js cargado (mejorado)');

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from((ctx || document).querySelectorAll(s));

/* ---------- UI helpers ---------- */
function showStatus(msg, { error = false, timeout = 4000 } = {}) {
  const el = qs('#forecast-status');
  if (!el) return;
  el.textContent = msg;
  el.style.color = error ? '#b91c1c' : '';
  if (timeout > 0) {
    setTimeout(() => { if (el) el.style.color = ''; }, timeout);
  }
}

function clearPreview() {
  const p = qs('#forecast-preview');
  const t = qs('#forecast-preview-text');
  if (!p || !t) return;
  t.textContent = '';
  p.hidden = true;
}

/* ---------- Form serialization & validation ---------- */
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

/* ---------- Submit ---------- */
async function submitForecast(endpoint, payload) {
  const res = await fetchWithAuth(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  return res;
}

/* ---------- Handlers ---------- */
async function onSubmitCreate(e) {
  e.preventDefault();
  const form = qs('#forecast-form');
  if (!form) return;

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
      } catch (err) {
        msg = await res.text().catch(() => msg);
      }
      throw new Error(msg);
    }

    showStatus('Forecast creado correctamente', { error: false, timeout: 1500 });
    form.reset();
    clearPreview();

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

/* ---------- TZ + periodicity helpers ---------- */
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

  if (!hourly) return;

  // Si no existe el select, mantenemos ocultos los campos por seguridad
  if (!sel) {
    hourly.hidden = true;
    hourly.setAttribute('aria-hidden', 'true');
    return;
  }

  // Normalizar el value para evitar problemas con displayName vs name()
  const raw = String(sel.value ?? '').trim();
  const val = raw.toUpperCase(); // esperamos "HOURLY" como name() del enum

  const isHourly = val === 'HOURLY';

  // Mostrar/ocultar de forma robusta
  hourly.hidden = !isHourly;
  hourly.setAttribute('aria-hidden', (!isHourly).toString());

  // Si ocultamos, limpiamos los valores de hora para evitar envíos inválidos
  if (!isHourly) {
    const s = qs('#forecast-start-hour');
    const e = qs('#forecast-end-hour');
    if (s) s.value = '';
    if (e) e.value = '';
  }

  console.debug('toggleHourlyFields -> periodicity=', raw, 'isHourly=', isHourly);
}


function initPeriodicityToggle() {
  const sel = qs('#forecast-periodicity');
  const hourly = qs('.hour-fields');

  // Asegurar estado inicial (en caso de que el script se ejecute antes o después del DOM)
  if (hourly) {
    // Si la vista dejó hidden en el HTML, mantenemos oculto hasta comprobar
    hourly.hidden = true;
    hourly.setAttribute('aria-hidden', 'true');
  }

  if (!sel) {
    console.debug('initPeriodicityToggle: #forecast-periodicity no encontrado');
    return;
  }

  // Ligar ambos eventos para mayor compatibilidad (change y input)
  sel.addEventListener('change', toggleHourlyFields);
  sel.addEventListener('input', toggleHourlyFields);

  // Ejecutar una vez para fijar el estado inicial (útil si hay prefill)
  try {
    toggleHourlyFields();
  } catch (err) {
    console.error('toggleHourlyFields error', err);
  }
}


/* ---------- Fetch helpers for cascading selects ---------- */
async function fetchJson(url) {
  const res = await fetchWithAuth(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status} ${text}`);
  }
  return res.json();
}

function safeGetId(obj) {
  if (obj == null) return null;
  return obj.id ?? obj.projectId ?? obj.siteId ?? obj.value ?? null;
}
function safeGetName(obj) {
  if (obj == null) return null;
  return obj.name ?? obj.title ?? obj.projectName ?? obj.siteName ?? String(safeGetId(obj));
}

async function fetchAndPopulateProjects(clientId) {
  const projectSel = qs('#forecast-project');
  const siteSel = qs('#forecast-site');
  if (!projectSel) return;

  // Save any initial desired selection coming from form attributes:
  const form = qs('#forecast-form');
  const desiredProject = form?.dataset?.initialProject ?? qs('#prefill-project')?.value ?? null;

  // reset
  projectSel.innerHTML = '<option value="">-- seleccionar --</option>';
  projectSel.disabled = true;
  if (siteSel) {
    siteSel.innerHTML = '<option value="">-- (opcional) --</option>';
    siteSel.disabled = true;
  }

  if (!clientId) {
    projectSel.disabled = true;
    return;
  }

  try {
    const url = `/api/clients/${encodeURIComponent(String(clientId))}/projects`;
    const raw = await fetchJson(url);
    // support payloads like { data: [...] } or an array directly
    const projects = Array.isArray(raw) ? raw : (Array.isArray(raw?.data) ? raw.data : []);
    if (!Array.isArray(projects) || projects.length === 0) {
      projectSel.disabled = true;
      console.debug('fetchAndPopulateProjects: no projects for client', clientId, raw);
      return;
    }

    projects.forEach(p => {
      const id = safeGetId(p);
      const name = safeGetName(p);
      if (id == null) return;
      const opt = document.createElement('option');
      opt.value = id;
      opt.textContent = name;
      projectSel.appendChild(opt);
    });

    projectSel.disabled = false;

    // try to select desiredProject (from dataset or hidden input) or preserve if already selected
    const toSelect = (projectSel.value && projectSel.value !== '') ? projectSel.value : (desiredProject ?? '');
    if (toSelect) {
      const opt = projectSel.querySelector(`option[value="${toSelect}"]`);
      if (opt) {
        opt.selected = true;
      }
    }

  } catch (err) {
    console.error('fetchAndPopulateProjects error', err);
    projectSel.disabled = true;
  }
}

async function fetchAndPopulateSites(projectId) {
  const siteSel = qs('#forecast-site');
  if (!siteSel) return;

  const form = qs('#forecast-form');
  const desiredSite = form?.dataset?.initialSite ?? qs('#prefill-site')?.value ?? null;

  siteSel.innerHTML = '<option value="">-- (opcional) --</option>';
  siteSel.disabled = true;

  if (!projectId) {
    siteSel.disabled = true;
    return;
  }

  try {
    const url = `/api/projects/${encodeURIComponent(String(projectId))}/sites`;
    const raw = await fetchJson(url);
    const sites = Array.isArray(raw) ? raw : (Array.isArray(raw?.data) ? raw.data : []);
    if (!Array.isArray(sites) || sites.length === 0) {
      siteSel.disabled = true;
      console.debug('fetchAndPopulateSites: no sites for project', projectId, raw);
      return;
    }

    sites.forEach(s => {
      const id = safeGetId(s);
      const name = safeGetName(s);
      if (id == null) return;
      const opt = document.createElement('option');
      opt.value = id;
      opt.textContent = name;
      siteSel.appendChild(opt);
    });

    siteSel.disabled = false;

    const toSelect = (siteSel.value && siteSel.value !== '') ? siteSel.value : (desiredSite ?? '');
    if (toSelect) {
      const opt = siteSel.querySelector(`option[value="${toSelect}"]`);
      if (opt) opt.selected = true;
    }

  } catch (err) {
    console.error('fetchAndPopulateSites error', err);
    siteSel.disabled = true;
  }
}

/* ---------- Cascading selects init ---------- */
function initCascadingSelects() {
  const clientSel = qs('#forecast-client');
  const projectSel = qs('#forecast-project');

  if (clientSel) {
    clientSel.addEventListener('change', async (ev) => {
      const clientId = ev.target.value;
      // fetch projects for selected client
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

  // If a client is preselected on load, populate its projects
  const clientSelInitial = qs('#forecast-client');
  if (clientSelInitial && clientSelInitial.value) {
    // small timeout to ensure DOM other scripts finished
    setTimeout(() => fetchAndPopulateProjects(clientSelInitial.value).then(() => {
      // if a project is preselected (server might set it), populate sites
      const projectSelNow = qs('#forecast-project');
      if (projectSelNow && projectSelNow.value) {
        fetchAndPopulateSites(projectSelNow.value);
      } else {
        // also check for dataset initialProject on form
        const initialProject = qs('#forecast-form')?.dataset?.initialProject;
        if (initialProject) setTimeout(() => fetchAndPopulateSites(initialProject), 200);
      }
    }).catch(err => console.debug('init cascading initial load err', err)), 50);
  }
}

/* ---------- Bindings ---------- */
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

/* ---------- Auto-init ---------- */
(function init() {
  bindCreateForm();
})();