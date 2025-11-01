import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

console.log('create-employee.js cargado');

const qs  = (s) => document.querySelector(s);

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
  const nationalityId        = qs('#employeeNationality')?.value?.trim() || null;
  const maritalStatus        = qs('#employeeMaritalStatus')?.value?.trim() || null;
  const studyLevel           = qs('#employeeStudyLevel')?.value?.trim() || null;
  
  // Profesiones asociadas (select múltiple)
  let professionIds = [];
  const professionsSelect = qs('#employeeProfessions');
  if (professionsSelect) {
    professionIds = Array.from(professionsSelect.selectedOptions).map(opt => opt.value).filter(Boolean);
  }

  const previtionalSystem    = qs('#employeePrevitionalSystem')?.value?.trim() || null;
  const pensionEntity       = qs('#employeePensionEntity')?.value?.trim() || null;  
  const healthSystem         = qs('#employeeHealthSystem')?.value?.trim() || null;
  const healthEntity         = qs('#employeeHealthEntity')?.value?.trim() || null;
  const paymentMethod        = qs('#employeePaymentMethod')?.value?.trim() || null;
  const bankId               = qs('#employeeBank')?.value?.trim() || null;
  const bankAccountType      = qs('#employeeBankAccountType')?.value?.trim() || null;
  const bankAccountNumber    = qs('#employeeBankAccountNumber')?.value?.trim() || null;
  const contractType         = qs('#employeeContractType')?.value?.trim() || null;
  const workSchedule         = qs('#employeeWorkSchedule')?.value?.trim() || null;
  const shiftSystem          = qs('#employeeShiftSystem')?.value?.trim() || null;
  const shiftPatternId       = qs('#employeeShiftPattern')?.value?.trim() || null;
  const positionId           = qs('#employeePosition')?.value?.trim() || null;
  const address              = qs('#employeeAddress')?.value?.trim() || null;
  const hireDate             = qs('#employeeHireDate')?.value || null;
  const birthDate            = qs('#employeeBirthDate')?.value || null;

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

  try {
    // FormData para soportar archivo y campos complejos
    const formData = new FormData();
    formData.append('name', name);

    if (fatherSurname) formData.append('fatherSurname', fatherSurname);
    if (motherSurname) formData.append('motherSurname', motherSurname);
    if (rut) formData.append('rut', rut);
    if (mail) formData.append('mail', mail);
    if (phone) formData.append('phone', phone);
    if (secondaryPhone) formData.append('secondaryPhone', secondaryPhone);
    if (gender) formData.append('gender', gender);
    if (nationalityId) formData.append('nationalityId', nationalityId);
    if (maritalStatus) formData.append('maritalStatus', maritalStatus);
    if (studyLevel) formData.append('studyLevel', studyLevel);
    if (Array.isArray(professionIds) && professionIds.length > 0) {
      professionIds.forEach(id => formData.append('professionIds', id)); // MULTI
    }
    if (previtionalSystem) formData.append('previtionalSystem', previtionalSystem);
    if (pensionEntity) formData.append('pensionEntity', pensionEntity);
    if (healthSystem) formData.append('healthSystem', healthSystem);
    if (healthEntity) formData.append('healthEntity', healthEntity);
    if (paymentMethod) formData.append('paymentMethod', paymentMethod);
    if (bankId) formData.append('bankId', bankId);
    if (bankAccountType) formData.append('bankAccountType', bankAccountType);
    if (bankAccountNumber) formData.append('bankAccountNumber', bankAccountNumber);
    if (contractType) formData.append('contractType', contractType);
    if (workSchedule) formData.append('workSchedule', workSchedule);
    if (shiftSystem) formData.append('shiftSystem', shiftSystem);
    if (shiftPatternId) formData.append('shiftPatternId', shiftPatternId);
    if (positionId) formData.append('positionId', positionId);
    if (address) formData.append('address', address);
    if (hireDate) formData.append('hireDate', hireDate);
    if (birthDate) formData.append('birthDate', birthDate);

    // Proyectos asociados (MULTI)
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
      // Vuelve al listado
      navigateTo('/private/employees/table-view');
    }, 600);
  } catch (e2) {
    if (err) err.textContent = e2.message;
  }
}

function onCancelCreate(e) {
  e.preventDefault();
  navigateTo('/private/employees/table-view');
}

/* --- Bindings --- */
function bindCreateEmployeeForm() {
  qs('#createEmployeeForm')?.addEventListener('submit', onSubmitCreate);
  qs('#cancelCreateEmployee')?.addEventListener('click', onCancelCreate);
}

/* --- init --- */
(function init() {
  bindCreateEmployeeForm();
})();