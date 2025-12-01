import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from(ctx.querySelectorAll(s));
const ROWS_FALLBACK_URL = '/private/site-supervision-visits/table-search';
const DEFAULT_VIEW = '/private/site-supervision-visits/table-view';

/* -------------------------
   Badge update & observers
   ------------------------- */
function updateVisitsCountFromTbody(tbody) {
  const tb = tbody || qs('.site-visit-table tbody');
  if (!tb) return;
  const empty = tb.querySelector('.empty');
  const count = empty ? 0 : tb.querySelectorAll('tr').length;
  const badge = qs('.site-visit-header .visits-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

/*
  Observe changes on the tbody (rows added/removed) and on the table container
  to gracefully reattach if the tbody node is replaced.
*/
let __siteVisit_tbodyObserver = null;
let __siteVisit_tableObserver = null;

function startObservingTbody() {
  stopObservingTbody();

  const tbody = qs('.site-visit-table tbody');
  const tableContainer = qs('.site-visit-table-container');

  if (tbody) {
    // immediate update
    updateVisitsCountFromTbody(tbody);

    __siteVisit_tbodyObserver = new MutationObserver((mutations) => {
      let shouldUpdate = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          shouldUpdate = true;
          break;
        }
      }
      if (shouldUpdate) updateVisitsCountFromTbody(tbody);
    });
    __siteVisit_tbodyObserver.observe(tbody, { childList: true, subtree: false });
  }

  // Observe the container in case tbody is replaced entirely (we'll re-init)
  if (tableContainer) {
    __siteVisit_tableObserver = new MutationObserver((mutations) => {
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
    __siteVisit_tableObserver.observe(tableContainer, { childList: true, subtree: true });
  }
}

function stopObservingTbody() {
  try {
    if (__siteVisit_tbodyObserver) { __siteVisit_tbodyObserver.disconnect(); __siteVisit_tbodyObserver = null; }
    if (__siteVisit_tableObserver) { __siteVisit_tableObserver.disconnect(); __siteVisit_tableObserver = null; }
  } catch (e) {
    // noop
  }
}

/* -------------------------
   Table delegation & fetch
   ------------------------- */
function initTableDelegation() {
  const container = qs('.site-visit-table-container');
  if (!container || container.__siteVisitDeleg) return;
  container.__siteVisitDeleg = true;
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

    const currentTbody = qs('.site-visit-table tbody');
    if (!currentTbody) {
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    // Replace rows robustly by replacing innerHTML (keeps tbody node the same)
    currentTbody.innerHTML = newTbody.innerHTML;

    // Replace header-meta if provided
    if (newHeaderMeta) {
      const currentHeader = qs('.site-visit-header .header-meta');
      if (currentHeader) currentHeader.replaceWith(newHeaderMeta);
    }

    // Always update badge (and observers will keep it in sync afterwards)
    updateVisitsCountFromTbody(currentTbody);

  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

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
      console.error('[site-visit] error fetching rows', err);
      const errDiv = document.getElementById('siteVisitSearchError') || (() => {
        const d = document.createElement('div'); d.id = 'siteVisitSearchError'; d.className = 'error';
        const header = qs('.site-visit-header'); if (header) header.insertAdjacentElement('afterend', d); else document.body.prepend(d);
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
    // Start observing tbody so visits-count updates automatically after AJAX replacements
    startObservingTbody();
  } catch (e) {
    console.error('[site-visit] init failed', e);
  }
})();