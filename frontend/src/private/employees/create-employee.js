import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-employee.js cargado');

const qs  = (s) => document.querySelector(s);

function openModal() {
  const m = qs('#createEmployeeModal');
  if (!m) return;
  m.classList.remove('hidden');
  m.setAttribute('aria-hidden', 'false');
  document.body.classList.add('no-scroll');
  setTimeout(() => qs('#employeeName')?.focus(), 0);
}

function closeModal() {
  const m = qs('#createEmployeeModal');
  if (!m) return;
  m.classList.add('hidden');
  m.setAttribute('aria-hidden', 'true');
  document.body.classList.remove('no-scroll');

  qs('#createEmployeeForm')?.reset();
  const msg = qs('#createEmployeeError');
  const ok  = qs('#createEmployeeOk');
  if (msg) msg.textContent = '';
  if (ok)  ok.style.display = 'none';
}

/* --- Crear empleado --- */
async function onSubmitCreate(e) {
  e.preventDefault();

  // Campos básicos
  const name                 = qs('#employeeName')?.value?.trim();
  const fatherSurname        = qs('#employeeFatherSurname')?.value?.trim() || null;
  const motherSurname        = qs('#employeeMotherSurname')?.value?.trim() || null;
  const rut                  = qs('#employeeRut')?.value?.trim() || null;
  const mail                 = qs('#employeeMail')?.value?.trim() || null;
  const phone                = qs('#employeePhone')?.value?.trim() || null;
  const secondaryPhone       = qs('#employeeSecondaryPhone')?.value?.trim() || null;
  const gender               = qs('#employeeGender')?.value?.trim() || null;
  const nationality          = qs('#employeeNationality')?.value?.trim() || null;
  const maritalStatus        = qs('#employeeMaritalStatus')?.value?.trim() || null;
  const studyLevel           = qs('#employeeStudyLevel')?.value?.trim() || null;
  const profession           = qs('#employeeProfession')?.value?.trim() || null;
  const previtionalSystem    = qs('#employeePrevitionalSystem')?.value?.trim() || null;
  const healthSystem         = qs('#employeeHealthSystem')?.value?.trim() || null;
  const paymentMethod        = qs('#employeePaymentMethod')?.value?.trim() || null;
  const bankName             = qs('#employeeBankName')?.value?.trim() || null;
  const bankAccountType      = qs('#employeeBankAccountType')?.value?.trim() || null;
  const bankAccountNumber    = qs('#employeeBankAccountNumber')?.value?.trim() || null;
  const contractType         = qs('#employeeContractType')?.value?.trim() || null;
  const workSchedule         = qs('#employeeWorkSchedule')?.value?.trim() || null;
  const shiftSystem          = qs('#employeeShiftSystem')?.value?.trim() || null;
  const position             = qs('#employeePosition')?.value?.trim() || null;
  const address              = qs('#employeeAddress')?.value?.trim() || null;
  const hireDate             = qs('#employeeHireDate')?.value || null;
  const birthDate            = qs('#employeeBirthDate')?.value || null;
  const exitDate             = qs('#employeeExitDate')?.value || null;
  const active               = !!qs('#employeeActive')?.checked;

  // Usuario asociado
  const userId = qs('#employeeUser')?.value || null;

  // Proyectos asociados (select múltiple)
  let projectIds = [];
  const projectsSelect = qs('#employeeProjects');
  if (projectsSelect) {
    projectIds = Array.from(projectsSelect.selectedOptions).map(opt => opt.value).filter(Boolean);
  }

  // Fotografía
  const photoInput = qs('#employeePhoto');
  let photo = null;
  if (photoInput && photoInput.files && photoInput.files[0]) {
    photo = photoInput.files[0];
  }

  const err = qs('#createEmployeeError');
  const ok  = qs('#createEmployeeOk');
  if (err) err.textContent = '';
  if (ok)  ok.style.display = 'none';

  if (!name) {
    if (err) err.textContent = 'El nombre es obligatorio.';
    return;
  }

  if (!userId) {
    if (err) err.textContent = 'Debe seleccionar un usuario asociado.';
    return;
  }

  try {
    // FormData para soportar archivo y campos complejos
    const formData = new FormData();
    formData.append('name', name);
    formData.append('fatherSurname', fatherSurname);
    formData.append('motherSurname', motherSurname);
    formData.append('rut', rut);
    formData.append('mail', mail);
    formData.append('phone', phone);
    formData.append('secondaryPhone', secondaryPhone);
    formData.append('gender', gender);
    formData.append('nationality', nationality);
    formData.append('maritalStatus', maritalStatus);
    formData.append('studyLevel', studyLevel);
    formData.append('profession', profession);
    formData.append('previtionalSystem', previtionalSystem);
    formData.append('healthSystem', healthSystem);
    formData.append('paymentMethod', paymentMethod);
    formData.append('bankName', bankName);
    formData.append('bankAccountType', bankAccountType);
    formData.append('bankAccountNumber', bankAccountNumber);
    formData.append('contractType', contractType);
    formData.append('workSchedule', workSchedule);
    formData.append('shiftSystem', shiftSystem);
    formData.append('position', position);
    formData.append('address', address);
    formData.append('hireDate', hireDate);
    formData.append('birthDate', birthDate);
    formData.append('exitDate', exitDate);
    formData.append('active', active);

    formData.append('userId', userId);

    // Para múltiples projectIds
    projectIds.forEach(id => formData.append('projectIds', id));

    if (photo) formData.append('photo', photo);

    // Ajusta el endpoint si en tu backend usas otro
    const res = await fetchWithAuth('/api/employees/create', {
      method: 'POST',
      body: formData
    });

    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      throw new Error(msg || 'No se pudo crear el empleado');
    }

    if (ok) ok.style.display = 'block';
    setTimeout(() => {
      closeModal();
      // Vuelve al listado
      navigateTo('/private/employees/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  }
}

/* --- Bindings --- */
function bindModal() {
  qs('#createEmployeeBtn')?.addEventListener('click', openModal);
  qs('#closeCreateEmployee')?.addEventListener('click', closeModal);
  qs('#cancelCreateEmployee')?.addEventListener('click', closeModal);
  qs('#createEmployeeForm')?.addEventListener('submit', onSubmitCreate);
  document.addEventListener('keydown', (ev) => { if (ev.key === 'Escape') closeModal(); });
}

/* --- init --- */
(function init() {
  bindModal();
})();