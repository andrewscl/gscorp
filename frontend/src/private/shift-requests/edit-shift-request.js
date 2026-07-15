import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import { displayAlert } from '../../shared/display-alert.js';

const qs = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
const externalId = qs('#shiftRequestExternalId')?.value;

console.log("edit-shift-request.js cargado");

async function onSaveClick(e) {
  const startDate = qs('#shiftRequestStartDate')?.value;
  const endDate = qs('#shiftRequestEndDate')?.value;
  const status = qs('#shiftRequestStatus')?.value;
  const description = qs('#shiftRequestDescription')?.value;

  const payload = { startDate, endDate, status, description};

  if (!externalId || !startDate || !endDate || !status) {
  console.log("falta información para efectuar la actualización.");
  return;
  }

  try {
    const url = `/api/shift-requests/${externalId}`;
    const res = await fetchWithAuth(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      let txt = '';
      try {
        txt = await res.text();
      } catch { throw new Error(txt || `Error ${res.status}`); }
    }

    displayAlert(alertSuccess, 'Cambios guardados', 1000);
    setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1000);
  } catch (err) {
    console.error('save error', err);
    displayAlert(alertError, "Error al guardar. " + err.message, 2000);
  }
}


const shiftsUpdate = async () => {
  try {
    const url = `/api/shifts/create/${externalId}`;

    const res = await fetchWithAuth(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(
        { clientTz: clientTz}
      )
    });

    console.log("Estado de la respuesta del backend:", res.status);

    if (!res.ok) {
      let errorMsg = 'Error en el servidor';
      try {
        const rawText = await res.clone().text();
        console.log("Cuerpo del error en bruto:", rawText);
        const data = JSON.parse(rawText);
        errorMsg = data.error || errorMsg;
      } catch (e) {
        try {
          const rawTextFallBack = await res.text();
          if (rawTextFallBack) errorMsg = rawTextFallBack;
        } catch {}
      }
      throw new Error(errorMsg);
    }

    displayAlert(alertSuccess,
                'Turnos generados correctamente', 1500);
    await loadLastShiftsTable();

  } catch (err) {
    console.error('save error', err);
    displayAlert(alertError, err.message, 2000);
  }
}

const loadLastShiftsTable = async () => {
    const shiftsToShow = 5;
    try {
        const response = await fetchWithAuth(`/api/shifts/last-shifts/${externalId}/${shiftsToShow}`, {
          method: 'GET',
          headers: { 'Accept': 'application/json' },
        });

        if (!response.ok) throw new Error("No se pudieron cargar los turnos");

        const pageData = await response.json();
        const shifts = pageData.content || [];

        const tbody = qs('#shifts-table-body'); 
        if (!tbody) return;

        tbody.innerHTML = ''; // Limpiamos la tabla
        if (shifts.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin turnos generados</td></tr>`;
            return;
        }

        shifts.forEach(shift => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${formatDate(shift.shiftDate)}</td>
                <td>${formatTime(shift.startTs)}</td>
                <td>${formatTime(shift.endTs)}</td>
                <td>-</td>
            `;
            tbody.appendChild(row);
        });

    } catch (err) {
        console.error("Error al renderizar los turnos:", err);
    }
}


// 🚀 Convierte "2026-07-15" a "15-07-2026"
const formatDate = (dateString) => {
    if (!dateString) return '—';
    const [year, month, day] = dateString.split('-');
    return `${day}-${month}-${year}`;
};

// 🚀 Extrae la hora "HH:MM" de un formato ISO/OffsetDateTime
const formatTime = (isoString) => {
    if (!isoString) return '—';
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
};


function onCancelClick(e) {
  displayAlert(alertWarning,
              'La edición del requerimiento ha sido cancelada', 1500);
  setTimeout(() => navigateTo('/private/shift-requests/table-view'), 1500);
}


function bindEditShiftRequest() {
    const saveBtn = qs('#submit');
    if (saveBtn) {
        saveBtn.addEventListener('click', onSaveClick);
    }
    const shiftsUpdateBtn = qs('#btnShiftsUpdate');
    if (shiftsUpdateBtn) {
        shiftsUpdateBtn.addEventListener('click', shiftsUpdate);
    }
    const cancelBtn = qs('#cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', onCancelClick);
    }
}


(function init() {
  bindEditShiftRequest();

})();