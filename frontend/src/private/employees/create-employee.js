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

  // Patrón de turno (nuevo)
  const shiftPatternId       = qs('#employeeShiftPattern')?.value?.trim() || null;

  // Cargo (position) (ID)
  const positionId           = qs('#employeePosition')?.value?.trim() || null;
  
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
    formData.append('nationalityId', nationalityId);
    formData.append('maritalStatus', maritalStatus);
    formData.append('studyLevel', studyLevel);
    professionIds.forEach(id => formData.append('professionIds', id)); // MULTI
    formData.append('previtionalSystem', previtionalSystem);
    formData.append('pensionEntity', pensionEntity);
    formData.append('healthSystem', healthSystem);
    formData.append('healthEntity', healthEntity);
    formData.append('paymentMethod', paymentMethod);
    formData.append('bankId', bankId);
    formData.append('bankAccountType', bankAccountType);
    formData.append('bankAccountNumber', bankAccountNumber);
    formData.append('contractType', contractType);
    formData.append('workSchedule', workSchedule);
    formData.append('shiftSystem', shiftSystem);
    formData.append('shiftPatternId', shiftPatternId); // NUEVO
    formData.append('positionId', positionId); // ID
    formData.append('address', address);
    formData.append('hireDate', hireDate);
    formData.append('birthDate', birthDate);
    formData.append('exitDate', exitDate);
    formData.append('active', active);

    formData.append('userId', userId);

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

/* --- Bindings --- */
function bindCreateEmployeeForm() {
  qs('#createEmployeeForm')?.addEventListener('submit', onSubmitCreate);
}

/* --- init --- */
(function init() {
  bindCreateEmployeeForm();
})();