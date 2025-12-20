import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from((ctx || document).querySelectorAll(s));
const ROWS_FALLBACK_URL = '/private/employees/table-search';
const DEFAULT_VIEW = '/private/employees/table-view';

/* -------------------------
   Badge update & observers
   ------------------------- */
function updateEmployeesCountFromTbody(tbody) {
  const tb = tbody || qs('.employees-table tbody');
  if (!tb) return;
  const empty = tb.querySelector('.empty');
  // count only direct tr children (avoid counting nested template rows if any)
  const rows = empty ? [] : Array.from(tb.children).filter(n => n.nodeName && n.nodeName.toLowerCase() === 'tr');
  const count = rows.length;
  const badge = qs('.employees-header .employees-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

/*
  Observe changes on the tbody (rows added/removed) and on the table container
  to gracefully reattach if the tbody node is replaced.
*/
let __employees_tbodyObserver = null;
let __employees_tableObserver = null;

function startObservingTbody() {
  stopObservingTbody();

  const tbody = qs('.employees-table tbody');
  const tableContainer = qs('.employees-table-container');

  if (tbody) {
    // immediate update
    updateEmployeesCountFromTbody(tbody);

    __employees_tbodyObserver = new MutationObserver((mutations) => {
      let shouldUpdate = false;
      for (const m of mutations) {
        if (m.type === 'childList' && (m.addedNodes.length || m.removedNodes.length)) {
          shouldUpdate = true;
          break;
        }
      }
      if (shouldUpdate) updateEmployeesCountFromTbody(tbody);
    });
    __employees_tbodyObserver.observe(tbody, { childList: true, subtree: false });
  }

  // Observe the container in case tbody is replaced entirely (we'll re-init)
  if (tableContainer) {
    __employees_tableObserver = new MutationObserver((mutations) => {
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
        // reattach observers after a short tick to allow DOM settle
        setTimeout(() => startObservingTbody(), 50);
      }
    });
    __employees_tableObserver.observe(tableContainer, { childList: true, subtree: true });
  }
}

function stopObservingTbody() {
  try {
    if (__employees_tbodyObserver) { __employees_tbodyObserver.disconnect(); __employees_tbodyObserver = null; }
    if (__employees_tableObserver) { __employees_tableObserver.disconnect(); __employees_tableObserver = null; }
  } catch (e) {
    // noop
  }
}

/* -------------------------
   Table delegation & fetch
   ------------------------- */
function initTableDelegation() {
  const container = qs('.employees-table-container');
  if (!container || container.__employeesDeleg) return;
  container.__employeesDeleg = true;
  container.addEventListener('click', (ev) => {
    const btn = ev.target.closest('.action-btn, a[data-path], button[data-path]');
    if (!btn) return;
    const path = btn.dataset.path || btn.getAttribute('href');
    if (!path) return;
    ev.preventDefault();
    navigateTo(path, true);
  });
}

async function fetchRows({ baseUrl, q, active, projectId, positionId, clientTz, applyBtn }) {
  const params = new URLSearchParams();
  if (q) params.set('q', q);
  if (active !== undefined && active !== null && String(active) !== '') params.set('active', String(active));
  if (projectId !== undefined && projectId !== null && String(projectId) !== '') params.set('projectId', String(projectId));
  if (positionId !== undefined && positionId !== null && String(positionId) !== '') params.set('positionId', String(positionId));
  if (clientTz) params.set('tz', clientTz);
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

    const currentTbody = qs('.employees-table tbody');
    if (!currentTbody) {
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    // Replace rows robustly by replacing innerHTML (keeps tbody node the same)
    currentTbody.innerHTML = newTbody.innerHTML;

    // Replace header-meta if provided
    if (newHeaderMeta) {
      const currentHeader = qs('.employees-header .header-meta');
      if (currentHeader) currentHeader.replaceWith(newHeaderMeta);
    }

    // Always update badge (and observers will keep it in sync afterwards)
    updateEmployeesCountFromTbody(currentTbody);

  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

function bindApplyButton() {
  const applyBtn = qs('#applyFiltersBtn');
  const qInput = qs('#filter-q');
  const statusSelect = qs('#filter-status');
  const projectSelect = qs('#filter-project');
  const positionSelect = qs('#filter-position');

  if (!applyBtn) return;

  if (!applyBtn.hasAttribute('type')) applyBtn.setAttribute('type', 'button');

  applyBtn.addEventListener('click', async (ev) => {
    ev.preventDefault();
    const q = qInput ? (qInput.value || '').trim() : '';
    const activeRaw = statusSelect ? statusSelect.value : '';
    // normalize active: '' -> null, 'true'/'false' -> boolean
    let active = null;
    if (activeRaw === 'true') active = true;
    else if (activeRaw === 'false') active = false;

    const projectId = projectSelect ? (projectSelect.value || '') : '';
    const positionId = positionSelect ? (positionSelect.value || '') : '';

    let clientTz = '';
    try { clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || ''; } catch (e) { clientTz = ''; }

    const baseUrl = applyBtn.dataset.ajaxUrl || applyBtn.dataset.path || ROWS_FALLBACK_URL;
    try {
      await fetchRows({ baseUrl, q, active, projectId, positionId, clientTz, applyBtn });
    } catch (err) {
      console.error('[employees] error fetching rows', err);
      const errDiv = document.getElementById('employeesTableSearchError') || (() => {
        const d = document.createElement('div'); d.id = 'employeesTableSearchError'; d.className = 'error';
        const header = qs('.employees-header'); if (header) header.insertAdjacentElement('afterend', d); else document.body.prepend(d);
        return d;
      })();
      errDiv.textContent = err.message || 'Error al obtener resultados.';
    }
  });

  // Optional: submit on Enter when focus inside search input
  if (qInput) {
    qInput.addEventListener('keydown', (ev) => {
      if (ev.key === 'Enter') {
        ev.preventDefault();
        applyBtn.click();
      }
    });
  }
}

/* init */
(function init() {
  try {
    initTableDelegation();
    bindApplyButton();
    // Start observing tbody so employees-count updates automatically after AJAX replacements
    startObservingTbody();
  } catch (e) {
    console.error('[employees] init failed', e);
  }
})();