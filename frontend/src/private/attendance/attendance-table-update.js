import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from(ctx.querySelectorAll(s));

const ROWS_FALLBACK_URL = '/private/attendance/attdc-filter';
const DEFAULT_VIEW = '/private/attendance/attendance-view';

/* -------------------------
   Badge update & observers
   ------------------------- */
function updateAttendanceCountFromTbody(tbody) {
  const tb = tbody || qs('.attendance-table tbody');
  if (!tb) return;
  const empty = tb.querySelector('.empty');
  // count only data rows (exclude rows used for "empty" message)
  const count = empty ? 0 : tb.querySelectorAll('tr').length;
  const badge = qs('.attendance-header .attendance-count') || qs('.attendance-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

let __attendance_tbodyObserver = null;
let __attendance_tableObserver = null;

function startObservingTbody() {
  stopObservingTbody();

  const tbody = qs('.attendance-table tbody');
  const tableContainer = qs('.attendance-table-container');

  if (tbody) {
    // initial update
    updateAttendanceCountFromTbody(tbody);

    __attendance_tbodyObserver = new MutationObserver((mutations) => {
      let shouldUpdate = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          shouldUpdate = true;
          break;
        }
      }
      if (shouldUpdate) updateAttendanceCountFromTbody(tbody);
    });
    __attendance_tbodyObserver.observe(tbody, { childList: true, subtree: false });
  }

  if (tableContainer) {
    __attendance_tableObserver = new MutationObserver((mutations) => {
      let tbodyReplaced = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          for (const n of [...m.addedNodes, ...m.removedNodes]) {
            if (n.nodeName && n.nodeName.toLowerCase() === 'tbody') {
              tbodyReplaced = true;
              break;
            }
          }
        }
        if (tbodyReplaced) break;
      }
      if (tbodyReplaced) {
        // reattach observers after a tick to allow DOM settle
        setTimeout(() => startObservingTbody(), 50);
      }
    });
    __attendance_tableObserver.observe(tableContainer, { childList: true, subtree: true });
  }
}

function stopObservingTbody() {
  try {
    if (__attendance_tbodyObserver) { __attendance_tbodyObserver.disconnect(); __attendance_tbodyObserver = null; }
    if (__attendance_tableObserver) { __attendance_tableObserver.disconnect(); __attendance_tableObserver = null; }
  } catch (e) {
    // noop
  }
}

/* -------------------------
   Table delegation & fetch
   ------------------------- */
function initTableDelegation() {
  const container = qs('.attendance-table-container');
  if (!container || container.__attendanceDeleg) return;
  container.__attendanceDeleg = true;
  container.addEventListener('click', (ev) => {
    const btn = ev.target.closest('.action-btn, a[data-path], button[data-path]');
    if (!btn) return;
    const path = btn.dataset.path || btn.getAttribute('href');
    if (!path) return;
    ev.preventDefault();
    navigateTo(path, true);
  });
}

async function fetchRows({ baseUrl, from, to, clientTz, applyBtn }) {
  const params = new URLSearchParams();
  if (from) params.set('from', from);
  if (to) params.set('to', to);
  if (clientTz) params.set('clientTz', clientTz);
  const url = baseUrl + (params.toString() ? `?${params.toString()}` : '');

  if (applyBtn) {
    applyBtn.disabled = true;
    applyBtn.classList.add('is-loading');
  }

  try {
    const res = await fetchWithAuth(url, {
      method: 'GET',
      credentials: 'same-origin',
      headers: { 'Accept': 'text/html', 'X-Requested-With': 'XMLHttpRequest' }
    });

    if (!res.ok) {
      if (res.status === 401 || res.status === 302) {
        navigateTo('/login', true);
        return;
      }
      const txt = await res.text().catch(() => '');
      throw new Error(txt || `HTTP ${res.status}`);
    }

    const html = await res.text();

    // Wrap to ensure tbody/tr parse correctly
    const tmp = document.createElement('div');
    tmp.innerHTML = '<table>' + html.trim() + '</table>';

    const newHeaderMeta = tmp.querySelector('.header-meta');
    let newTbody = tmp.querySelector('tbody');
    if (!newTbody) {
      // server may return only rows; gather tr nodes
      const rows = tmp.querySelectorAll('tr');
      newTbody = document.createElement('tbody');
      rows.forEach(r => newTbody.appendChild(r));
    }

    const currentTbody = qs('.attendance-table tbody');
    if (!currentTbody) {
      // can't find current table, navigate to default view
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    // Replace rows by replacing innerHTML on the current tbody node (keeps observers attached)
    currentTbody.innerHTML = newTbody.innerHTML;

    // Replace header-meta if provided
    if (newHeaderMeta) {
      const currentHeader = qs('.attendance-header .header-meta');
      if (currentHeader) currentHeader.replaceWith(newHeaderMeta);
    }

    // Update badge count
    updateAttendanceCountFromTbody(currentTbody);

  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

/* -------------------------
   Filters binding & quick ranges
   ------------------------- */
function bindApplyButton() {
  const applyBtn = qs('#applyFiltersBtn');
  const fromInput = qs('#filter-from');
  const toInput = qs('#filter-to');

  if (!applyBtn || !fromInput || !toInput) return;

  if (!applyBtn.hasAttribute('type')) applyBtn.setAttribute('type', 'button');

  applyBtn.addEventListener('click', async (ev) => {
    ev.preventDefault();
    let from = fromInput.value || '';
    let to = toInput.value || '';
    if (!from && !to) { navigateTo(DEFAULT_VIEW, true); return; }
    if (!from && to) from = to;
    if (!to && from) to = from;
    try {
      const f = new Date(from), t = new Date(to);
      if (!isNaN(f) && !isNaN(t) && f > t) [from, to] = [to, from];
    } catch (e) {}
    let clientTz = '';
    try { clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || ''; } catch (e) { clientTz = ''; }
    const baseUrl = applyBtn.dataset.ajaxUrl || applyBtn.dataset.path || ROWS_FALLBACK_URL;
    try {
      await fetchRows({ baseUrl, from, to, clientTz, applyBtn });
    } catch (err) {
      console.error('[attendance] error fetching rows', err);
      const errDiv = document.getElementById('attendanceSearchError') || (() => {
        const d = document.createElement('div'); d.id = 'attendanceSearchError'; d.className = 'error';
        const header = qs('.attendance-header'); if (header) header.insertAdjacentElement('afterend', d); else document.body.prepend(d);
        return d;
      })();
      errDiv.textContent = err.message || 'Error al obtener resultados.';
    }
  });
}

function isoDate(d) {
  // returns yyyy-mm-dd for Date
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function startQuickRangeButtons() {
  const group = qs('.quick-range');
  if (!group) return;
  group.addEventListener('click', (ev) => {
    const btn = ev.target.closest('button[data-range]');
    if (!btn) return;
    const range = btn.dataset.range;
    const fromInput = qs('#filter-from');
    const toInput = qs('#filter-to');
    if (!fromInput || !toInput) return;

    const now = new Date();
    let fromDate = new Date(now);
    let toDate = new Date(now);

    if (range === 'today') {
      // same day
    } else if (range === 'week') {
      // ISO week start (Monday)
      const day = now.getDay(); // 0 Sun .. 6 Sat
      const diffToMon = (day === 0) ? -6 : (1 - day);
      fromDate.setDate(now.getDate() + diffToMon);
    } else if (range === 'month') {
      fromDate = new Date(now.getFullYear(), now.getMonth(), 1);
    }

    fromInput.value = isoDate(fromDate);
    toInput.value = isoDate(toDate);

    // trigger apply
    const applyBtn = qs('#applyFiltersBtn');
    if (applyBtn) applyBtn.click();
  });
}

/* init */
(function init() {
  try {
    initTableDelegation();
    bindApplyButton();
    startQuickRangeButtons();
    startObservingTbody();
  } catch (e) {
    console.error('[attendance] init failed', e);
  }
})();