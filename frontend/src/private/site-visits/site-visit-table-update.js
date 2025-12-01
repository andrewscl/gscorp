// Adaptación mínima: usa data-ajax-url (o data-path) del botón si existe,
// y solo si no existe usa ROWS_URL por defecto.

import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const ROWS_URL = '/private/site-supervision-visits/table-search'; // fallback si no hay data-ajax-url
const DEFAULT_VIEW = '/private/site-supervision-visits/table-view';

function qs(sel, ctx = document) { return ctx.querySelector(sel); }

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

    let newTbody = tmp.querySelector('tbody');
    if (!newTbody) {
      const fragmentRows = tmp.querySelectorAll('tr');
      newTbody = document.createElement('tbody');
      fragmentRows.forEach(r => newTbody.appendChild(r));
    }

    const currentTbody = qs('.site-visit-table tbody');
    if (!currentTbody) {
      throw new Error('No se encontró tbody en la tabla actual (selector .site-visit-table tbody)');
    }

    currentTbody.replaceWith(newTbody);

    updateVisitsCountFromTbody();
    rebindTableActions();

  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.classList.remove('is-loading');
    }
  }
}

function updateVisitsCountFromTbody() {
  const tbody = document.querySelector('.site-visit-table tbody');
  if (!tbody) return;
  const empty = tbody.querySelector('.empty');
  const count = empty ? 0 : tbody.querySelectorAll('tr').length;
  const badge = document.querySelector('.site-visit-header .visits-count');
  if (badge) badge.textContent = `${count} registro${count === 1 ? '' : 's'}`;
}

function rebindTableActions() {
  document.querySelectorAll('.site-visit-table .action-btn').forEach(btn => {
    if (btn.__navBound) return;
    btn.__navBound = true;
    btn.addEventListener('click', (ev) => {
      const path = btn.getAttribute('data-path');
      if (path) navigateTo(path, true);
    });
  });
}

document.addEventListener('DOMContentLoaded', () => {
  const applyBtn = qs('#applyFiltersBtn');
  const fromInput = qs('#filter-from');
  const toInput = qs('#filter-to');

  if (!applyBtn || !fromInput || !toInput) return;

  applyBtn.addEventListener('click', async () => {
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
    } catch (e) {}

    let clientTz = '';
    try { clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || ''; } catch (e) { clientTz = ''; }

    // Obtener la URL desde el atributo del botón si está presente
    const baseUrl = applyBtn.dataset.ajaxUrl || applyBtn.dataset.path || ROWS_URL;

    try {
      await fetchRows({ baseUrl, from, to, clientTz, applyBtn });
    } catch (err) {
      console.error('Error al obtener filas:', err);
      const errDiv = document.getElementById('siteVisitSearchError') || (function () {
        const d = document.createElement('div'); d.id = 'siteVisitSearchError'; d.className = 'error';
        const header = qs('.site-visit-header'); if (header) header.insertAdjacentElement('afterend', d); else document.body.prepend(d);
        return d;
      })();
      errDiv.textContent = err.message || 'Error al obtener resultados.';
    }
  });
});