import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createShiftRequestModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#shiftRequestCode')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createShiftRequestModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createShiftRequestForm')?.reset();
  const err = qs('#createShiftRequestError');
  const ok  = qs('#createShiftRequestOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';
}

/** (Optional) If the <select> comes empty, try to load sites via API */
async function maybeLoadSites() {
  const sel = qs('#shiftRequestSiteId');
  if (!sel) return;

  const hasOptions = sel.querySelectorAll('option').length > 1;
  if (hasOptions) return;

  try {
    const res = await fetchWithAuth('/api/sites/all');
    if (!res.ok) return;

    const data = await res.json(); // [{id, name, ...}]
    // Clean and re-render
    sel.innerHTML = '<option value="" disabled selected>Select a site…</option>';
    data.forEach(s => {
      const opt = document.createElement('option');
      opt.value = String(s.id);
      opt.textContent = s.name;
      sel.appendChild(opt);
    });
  } catch { /* noop */ }
}

async function onClickCreate() {
  openModal();
  await maybeLoadSites();
}

async function onSubmitCreate(e) {
  e.preventDefault();

  const err = qs('#createShiftRequestError');
  const ok  = qs('#createShiftRequestOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  const code         = qs('#shiftRequestCode')?.value?.trim();
  const siteId       = Number(qs('#shiftRequestSite')?.value);
  const type         = qs('#shiftRequestType')?.value;
  const startDate    = qs('#shiftRequestStartDate')?.value;
  const endDate      = qs('#shiftRequestEndDate')?.value;
  const weekDays     = qs('#shiftRequestWeekDays')?.value?.trim() || null;
  const shiftDateTime= qs('#shiftRequestShiftDateTime')?.value || null;
  const status       = qs('#shiftRequestStatus')?.value;
  const description  = qs('#shiftRequestDescription')?.value?.trim() || null;

  // Minimal validation
  if (!code)      { err && (err.textContent = 'Code is required.'); return; }
  if (!siteId)    { err && (err.textContent = 'You must select a site.'); return; }
  if (!type)      { err && (err.textContent = 'Type is required.'); return; }
  if (!startDate) { err && (err.textContent = 'Start date is required.'); return; }
  if (!endDate)   { err && (err.textContent = 'End date is required.'); return; }
  if (type === 'SPORADIC' && !shiftDateTime) {
    err && (err.textContent = 'Shift DateTime is required for SPORADIC requests.');
    return;
  }

  // Disable submit during POST
  const submitBtn = e.submitter || qs('#createShiftRequestForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/shift-requests/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        code,
        siteId,
        type,
        startDate,
        endDate,
        weekDays,
        shiftDateTime: shiftDateTime ? shiftDateTime : null,
        status,
        description
      })
    });

    if (!res.ok) {
      let msg = '';
      try { msg = await res.text(); } catch {}
      if (!msg) msg = `Error ${res.status}`;
      throw new Error(msg);
    }

    ok && (ok.style.display = 'block');

    setTimeout(() => {
      closeModal();
      navigateTo('/private/shift-requests/table-view');
    }, 600);

  } catch (e2) {
    err && (err.textContent = e2.message || 'Could not create shift request');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

function bindModal() {
  qs('#createShiftRequestBtn')?.addEventListener('click', onClickCreate);
  qs('#closeCreateShiftRequest')?.addEventListener('click', closeModal);
  qs('#cancelCreateShiftRequest')?.addEventListener('click', closeModal);
  qs('#createShiftRequestForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

(function init() {
  bindModal();
})();