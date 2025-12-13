import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from(ctx.querySelectorAll(s));

const ROWS_FALLBACK_URL = '/private/shift-requests/table-search';
const DEFAULT_VIEW = '/private/shift-requests/table-view';

/* -------------------------
   Badge update & observers
   ------------------------- */
function updateRequestsCountFromTbody(tbody) {
  const tb = tbody || qs('.shift-requests-table tbody');
  if (!tb) return;
  const empty = tb.querySelector('.empty');
  const count = empty ? 0 : tb.querySelectorAll('tr').length;
  const badge = qs('.shift-requests-header .requests-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

/*
  Observe changes on the tbody (rows added/removed) and on the table container
  to gracefully reattach if the tbody node is replaced.
*/
let __shiftRequests_tbodyObserver = null;
let __shiftRequests_tableObserver = null;

function startObservingTbody() {
  stopObservingTbody();

  const tbody = qs('.shift-requests-table tbody');
  const tableContainer = qs('.shift-requests-table-container');

  if (tbody) {
    // immediate update
    updateRequestsCountFromTbody(tbody);

    __shiftRequests_tbodyObserver = new MutationObserver((mutations) => {
      let shouldUpdate = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          shouldUpdate = true;
          break;
        }
      }
      if (shouldUpdate) updateRequestsCountFromTbody(tbody);
    });
    __shiftRequests_tbodyObserver.observe(tbody, { childList: true, subtree: false });
  }

  // Observe the container in case tbody is replaced entirely (we'll re-init)
  if (tableContainer) {
    __shiftRequests_tableObserver = new MutationObserver((mutations) => {
      let tbodyReplaced = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          // If a tbody node was removed/added, re-init
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
        // reattach observers after a short tick to allow DOM settle
        setTimeout(() => startObservingTbody(), 50);
      }
    });
    __shiftRequests_tableObserver.observe(tableContainer, { childList: true, subtree: true });
  }
}

function stopObservingTbody() {
  try {
    if (__shiftRequests_tbodyObserver) { __shiftRequests_tbodyObserver.disconnect(); __shiftRequests_tbodyObserver = null; }
    if (__shiftRequests_tableObserver) { __shiftRequests_tableObserver.disconnect(); __shiftRequests_tableObserver = null; }
  } catch (e) {
    // noop
  }
}

/* -------------------------
   Table delegation & fetch
   ------------------------- */
function initTableDelegation() {
  const container = qs('.shift-requests-table-container');
  if (!container || container.__shiftRequestsDeleg) return;
  container.__shiftRequestsDeleg = true;
  container.addEventListener('click', (ev) => {
    const btn = ev.target.closest('.action-btn, a[data-path], button[data-path]');
    if (!btn) return;
    const path = btn.dataset.path || btn.getAttribute('href');
    if (!path) return;
    ev.preventDefault();
    navigateTo(path, true);
  });
}

async function fetchRows({ baseUrl, from, to, clientTz, siteId, type, applyBtn }) {
  const params = new URLSearchParams();
  if (from) params.set('from', from);
  if (to) params.set('to', to);
  if (clientTz) params.set('clientTz', clientTz);
  if (siteId) params.set('siteId', siteId);
  if (type) params.set('type', type);
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

    // wrap in a table so tbody/tr parse correctly
    const tmp = document.createElement('div');
    tmp.innerHTML = '<table>' + html.trim() + '</table>';

    const newHeaderMeta = tmp.querySelector('.header-meta');
    let newTbody = tmp.querySelector('tbody');
    if (!newTbody) {
      const rows = tmp.querySelectorAll('tr');
      newTbody = document.createElement('tbody');
      rows.forEach(r => newTbody.appendChild(r));
    }

    const currentTbody = qs('.shift-requests-table tbody');
    if (!currentTbody) {
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    // Replace rows robustly by replacing innerHTML (keeps tbody node the same)
    currentTbody.innerHTML = newTbody.innerHTML;

    // Replace header-meta if provided
    if (newHeaderMeta) {
      const currentHeader = qs('.shift-requests-header .header-meta');
      if (currentHeader) currentHeader.replaceWith(newHeaderMeta);
    }

    // Always update badge (and observers will keep it in sync afterwards)
    updateRequestsCountFromTbody(currentTbody);

  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

/* -------------------------
   Helpers to read filter inputs
   ------------------------- */
function readFilterValue() {
  // Flexible detection: try common selectors for site/type controls
  const siteEl = qs('#filter-site') || qs('[name="siteId"]') || qs('[data-filter="siteId"]');
  const typeEl = qs('#filter-type') || qs('[name="type"]') || qs('[data-filter="type"]');

  const siteVal = siteEl ? (siteEl.value || '') : '';
  const typeVal = typeEl ? (typeEl.value || '') : '';

  return { siteId: siteVal, type: typeVal };
}

/* -------------------------
   Bind apply button
   ------------------------- */
function bindApplyButton() {
  const applyBtn = qs('#applyShiftFiltersBtn');
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

    const { siteId, type } = readFilterValue();

    const baseUrl = applyBtn.dataset.ajaxUrl || applyBtn.dataset.path || ROWS_FALLBACK_URL;
    try {
      await fetchRows({ baseUrl, from, to, clientTz, siteId, type, applyBtn });
    } catch (err) {
      console.error('[shift-requests] error fetching rows', err);
      const errDiv = document.getElementById('shiftRequestSearchError') || (() => {
        const d = document.createElement('div'); d.id = 'shiftRequestSearchError'; d.className = 'error';
        const header = qs('.shift-requests-header'); if (header) header.insertAdjacentElement('afterend', d); else document.body.prepend(d);
        return d;
      })();
      errDiv.textContent = err.message || 'Error al obtener resultados.';
    }
  });
}

/* init */
(function init() {
  try {
    initTableDelegation();
    bindApplyButton();
    // Start observing tbody so requests-count updates automatically after AJAX replacements
    startObservingTbody();
  } catch (e) {
    console.error('[shift-requests] init failed', e);
  }
})();