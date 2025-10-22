import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createBankModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#bankName')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createBankModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createBankForm')?.reset();
  const err = qs('#createBankError');
  const ok  = qs('#createBankOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';
}

async function onClickCreateBank() {
  openModal();
}

async function onSubmitCreateBank(e) {
  e.preventDefault();

  const err = qs('#createBankError');
  const ok  = qs('#createBankOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  const name     = qs('#bankName')?.value?.trim();
  const code     = qs('#bankCode')?.value?.trim();
  const active   = !!qs('#bankActive')?.checked;
  const logoUrl  = qs('#bankLogoUrl')?.value?.trim() || null; // si tienes campo para URL

  if (!name)      { err && (err.textContent = 'El nombre del banco es obligatorio.'); return; }
  if (!code)      { err && (err.textContent = 'El código del banco es obligatorio.'); return; }

  // Deshabilita submit durante el POST
  const submitBtn = e.submitter || qs('#createBankForm button[type="submit"]');
  submitBtn && (submitBtn.disabled = true);

  try {
    const res = await fetchWithAuth('/api/banks/create', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        name,
        code,
        logoUrl,
        active
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
      navigateTo('/private/banks/table-view');
    }, 600);

  } catch (e2) {
    err && (err.textContent = e2.message || 'No se pudo crear el banco');
  } finally {
    submitBtn && (submitBtn.disabled = false);
  }
}

function bindModal() {
  qs('#createBankBtn')?.addEventListener('click', onClickCreateBank);
  qs('#closeCreateBank')?.addEventListener('click', closeModal);
  qs('#cancelCreateBank')?.addEventListener('click', closeModal);
  qs('#createBankForm')?.addEventListener('submit', onSubmitCreateBank);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

(function init() {
  bindModal();
})();