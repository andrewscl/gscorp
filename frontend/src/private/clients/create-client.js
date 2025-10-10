// /js/private/admin/clients/create-client.js
import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-client.js cargado');

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createClientModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#clientName')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createClientModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createClientForm')?.reset();
  const msg = qs('#createClientError');
  const ok  = qs('#createClientOk');
  if (msg) msg.textContent = '';
  if (ok)  ok.style.display = 'none';
}

/* --- Crear cliente --- */
async function onSubmitCreate(e) {
  e.preventDefault();

  
  const name        = qs('#clientName')?.value?.trim();
  const legalName   = qs('#clientLegalName')?.value?.trim() || null;
  const taxId       = qs('#clientTaxId')?.value?.trim() || null;
  const contactEmail= qs('#clientEmail')?.value?.trim() || null;
  const contactPhone= qs('#clientPhone')?.value?.trim() || null;
  const active      = !!qs('#clientActive')?.checked;

  const err = qs('#createClientError');
  const ok  = qs('#createClientOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre es obligatorio.';
    return;
  }

  try {
    // Ajusta el endpoint si en tu backend usas otro (p.ej. /api/admin/clients/create)
    const res = await fetchWithAuth('/api/clients/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, legalName, taxId, contactEmail, contactPhone, active })
    });

    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      throw new Error(msg || 'No se pudo crear el cliente');
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      closeModal();
      // Vuelve al listado
      navigateTo('/private/admin/clients');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  }
}

/* --- Bindings --- */
function bindModal() {
  qs('#createClientBtn')?.addEventListener('click', openModal);
  qs('#closeCreateClient')?.addEventListener('click', closeModal);
  qs('#cancelCreateClient')?.addEventListener('click', closeModal);
  qs('#createClientForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

/* --- init --- */
(function init() {
  bindModal();
})();
