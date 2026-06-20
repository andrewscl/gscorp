import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';
import loadGoogleMapsAPI from '../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../shared/maps/advanced-marker.js';
import { displayAlert } from '../../shared/display-alert.js';

let currentExecutionId = null;
let currentExecutionData = null;
let timerInterval = null;

const qs  = (s) => document.querySelector(s);
const alertSuccess = qs('.alert-success');
const alertError = qs('.alert-error');
const alertWarning = qs('.alert-warning');
const alertInfo = qs('.alert-info');

const scanBtnExecute = async () => {
    try {
        displayAlert(alertSuccess, 'Escáner activado correctamente', 2000);
    } catch (error) {
        console.error('Error al escanear checkpoint:', error);
    }
};

const incidentBtnExecute = async () => {
        setTimeout(() => navigateTo('/private/incidents/dashboard'), 1500);
}

let patrolInfo = null;
const endBtnExecute = async () => {
    const scanBtn = qs('.btn-scan');
    if(scanBtn) scanBtn.disabled = true;

    try {
        const response = 
            await fetchWithAuth(`/api/patrols/executions/${siteExternalId}/finish`, {
                    method: 'POST',
                    credentials: 'same-origin'
                });

        if (!response.ok) {
            throw new Error(`No fue posible cerrar la ronda: ${response.status}`);
        }

        displayAlert(alertSuccess, 'Ronda finalizada con exito.', 3000);
        setTimeout(() => navigateTo('/private/patrol/dashboard'), 3000);

    } catch (e) {
        console.error('Error al finalizar la patrulla:', error);
        displayAlert('error', 'No se pudo cerrar la patrulla actual.', 4000);
    }

}

function startTimer(startedAt, timerElement) {
    if (!timerElement) return;
    if (timerInterval) clearInterval(timerInterval);

    const startTime = new Date(startedAt).getTime();

    timerInterval = setInterval(() => {
        const difference = new Date().getTime() - startTime;
        if (difference < 0) return;

        const hours = String(Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))).padStart(2, '0');
        const minutes = String(Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0');
        const seconds = String(Math.floor((difference % (1000 * 60)) / 1000)).padStart(2, '0');

        timerElement.textContent = `⏱️ ${hours}:${minutes}:${seconds}`;
    }, 1000);
}


function bindEvents() {
    const scanBtn = qs('.btn-scan');
    if (scanBtn) {
        scanBtn.addEventListener('click', scanBtnExecute);
    }
    const incidentBtn = qs('.btn-incident');
    if (incidentBtn) {
        incidentBtn.addEventListener('click', incidentBtnExecute);
    }
    const endBtn = qs('.btn-end');
    if (endBtn) {
        endBtn.addEventListener('click', endBtnExecute);
    }
}


let patrolCheckpointsList = [];
const addPatrolCheckpoints = async () => {
    try {
        const res = await fetchWithAuth(`/api/patrol-chekpoints/${patrolExternalId}`, {
                credentials: 'same-origin'
        });

        if (!res.ok) throw new Error(`Error cargando checkpoints: ${res.status}`);

        patrolCheckpointsList = await res.json();

    } catch (e) {
        console.error("No se pudo cargar la lista de checkpoints:", e);
        patrolCheckpointsList = [];
    }
}



(async function init() {
  
    // extrae el ID del fragment
    currentExecutionId = container.dataset.patrolId;
    if (!currentExecutionId) {
        console.error('Atributo "data-patrol-id" ausente.');
        navigateTo('/private/employees/dashboard');
        return;
    }

    bindEvents();

    try {
        const response = await fetchWithAuth(`/api/patrols/executions/${currentExecutionId}`, {
            credentials: 'same-origin'
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        currentExecutionData = await response.json();

        const timerElement = qs('#patrol-timer', container);
        if (timerElement && currentExecutionData.startedAt) {
            startTimer(currentExecutionData.startedAt, timerElement);
        }
    } catch (error) {
        console.error('Error al inicializar datos de patrulla activa:', error);
        displayAlert('error', 'Error al sincronizar el estado de la ronda.', 4000);
    }

    console.log('patrol-execute initialized.');

})();