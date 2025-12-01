import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs = (s, ctx = document) => ctx.querySelector(s);
const qsa = (s, ctx = document) => Array.from(ctx.querySelectorAll(s));

const ROWS_FALLBACK_URL = '/private/site-supervision-visits/table-search';
const DEFAULT_VIEW = '/private/site-supervision-visits/table-view';

function updateVisitsCountFromTbody(tbody) {
  const tb = tbody || qs('.site-visit-table tbody');
  if (!tb) return;
  const empty = tb.querySelector('.empty');
  const count = empty ? 0 : tb.querySelectorAll('tr').length;
  const badge = qs('.site-visit-header .visits-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

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
    const tmp = document.createElement('div');
    tmp.innerHTML = html.trim();

    const newHeaderMeta = tmp.querySelector('.header-meta');
    let newTbody = tmp.querySelector('tbody');
    if (!newTbody) {
      const rows = tmp.querySelectorAll('tr');
      newTbody = document.createElement('tbody');
      rows.forEach(r => newTbody.appendChild(r));
    }

    const currentTbody = qs('.site-visit-table tbody');
    if (!currentTbody) {
      // Unexpected: no tbody to replace — fallback to full reload
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    if (newHeaderMeta) {
      const currentHeader = qs('.site-visit-header .header-meta');
      if (currentHeader) currentHeader.replaceWith(newHeaderMeta);
    }

    currentTbody.replaceWith(newTbody);

    // update badge if header wasn't returned
    if (!newHeaderMeta) updateVisitsCountFromTbody(newTbody);

    // delegation handles action clicks, no rebind needed
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

  if (!applyBtn || !fromInput || !toInput) {
    console.debug('[site-visit] apply button or inputs not found — skipping binding');
    return;
  }

  // ensure button won't accidentally submit a form if it's inside one
  if (!applyBtn.hasAttribute('type')) applyBtn.setAttribute('type', 'button');

  applyBtn.addEventListener('click', async (ev) => {
    ev.preventDefault();

    let from = fromInput.value || '';
    let to = toInput.value || '';

    if (!from && !to) {
      navigateTo(DEFAULT_VIEW, true);
      return;
    }

    if (!from && to) from = to;
    if (!to && from) to = from;

    try {
      const f = new Date(from), t = new Date(to);
      if (!isNaN(f) && !isNaN(t) && f > t) [from, to] = [to, from];
    } catch (e) { /* ignore */ }

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

/* Init similar pattern to your working modules */
(function init() {
  try {
    console.debug('[site-visit] initializing module');
    initTableDelegation();
    bindApplyButton();
    console.debug('[site-visit] initialized');
  } catch (e) {
    console.error('[site-visit] init failed', e);
  }
})();