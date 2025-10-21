import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-bank.js cargado');

const qs  = (s) => document.querySelector(s);

/* --- Crear banco --- */
async function onSubmitCreateBank(e) {
  e.preventDefault();

  const name    = qs('#bankName')?.value?.trim();
  const code    = qs('#bankCode')?.value?.trim();
  const active  = !!qs('#bankActive')?.checked;

  // Logo (opcional)
  const logoInput = qs('#bankLogo');
  let logo = null;
  if (logoInput && logoInput.files && logoInput.files[0]) {
    logo = logoInput.files[0];
  }

  const err = qs('#createBankError');
  const ok  = qs('#createBankOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre del banco es obligatorio.';
    return;
  }
  if (!code) {
    if (err) err.textContent = 'El código del banco es obligatorio.';
    return;
  }

  try {
    // FormData para soportar archivo (logo)
    const formData = new FormData();
    formData.append('name', name);
    formData.append('code', code);
    formData.append('active', active);
    if (logo) formData.append('logo', logo);

    // Ajusta el endpoint si tu backend usa otro
    const res = await fetchWithAuth('/api/banks/create', {
      method: 'POST',
      body: formData
    });

    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      throw new Error(msg || 'No se pudo crear el banco');
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      navigateTo('/private/banks/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  }
}

/* --- Bindings --- */
function bindCreateBankForm() {
  qs('#createBankForm')?.addEventListener('submit', onSubmitCreateBank);
}

function bindCancelCreateBank() {
  qs('#cancelCreateBank')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('/private/banks/table-view');
  });
}

/* --- init --- */
(function init() {
  bindCreateBankForm();
  bindCancelCreateBank();
})();