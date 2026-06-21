import { fetchWithAuth } from '../../../../auth.js';
import { navigateTo } from '../../../../navigation-handler.js';
import loadGoogleMapsAPI from '../../../../shared/maps/googlemaps-loader.js';
import { initMap } from '../../../../shared/maps/init-map.js';
import { addAdvancedMarker } from '../../../../shared/maps/advanced-marker.js';
import { displayAlert } from '../../../../shared/display-alert.js';

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
};


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

};


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
};


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
};


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
};


const renderHeader = (container, data) => {
    const titleElement = qs('#patrol-title', container);
    const locationElement = qs('#patrol-location', container);
    const timerElement = qs('#patrol-timer', container);
    const statusElement = qs('#patrol-status', container);

    if(titleElement && data.patrolName) {
        titleElement.textContent = data.patrolName;
    }
    if(locationElement && data.siteName) {
        locationElement.textContent = `📍 ${data.siteName}`; 
    }
    if (statusElement && data.status) {
        statusElement.textContent = data.status;
    }
    if(timerElement && data.startTime) {
        startTimer(data.startTime, timerElement);
    }
};


const renderProgress = (container, data) => {
    const progressText = qs('#progress-text', container);
    const progressBar = qs('#patrol-progress-bar', container);
    const timelineContainer = qs('#patrol-timeline-container', container);

    if (!data) return; 

    const totalPoints = data.checkpoints ? data.checkpoints.length : 0;
    const completedPoints = data.checkpoints ? data.checkpoints.filter(p => p.status === 'COMPLETED').length : 0;
    const percentage = totalPoints > 0 ? Math.round((completedPoints / totalPoints) * 100) : 0;

    // 1. Actualizar la barra superior dinámicamente
    if (progressText) {
        progressText.textContent = `${completedPoints} / ${totalPoints} Puntos`;
    }
    if (progressBar) {
        progressBar.style.width = `${percentage}%`;
        progressBar.setAttribute('aria-valuenow', percentage);
    }

    // 2. Renderizar las tarjetas con tu estructura nativa
    if (timelineContainer && data.checkpoints) {
        
        // Buscamos cuál es el primer punto pendiente para marcarlo como activo
        const firstPendingIndex = data.checkpoints.findIndex(p => p.status !== 'COMPLETED');

        timelineContainer.innerHTML = data.checkpoints.map((point, index) => {
            const isCompleted = point.status === 'COMPLETED';
            const isActive = index === firstPendingIndex;

            // Determinamos la clase de estado y el contenido del badge según tu SCSS
            let itemStatusClass = 'is-pending';
            let badgeContent = '○'; // Círculo neutro de texto para pendientes

            if (isCompleted) {
                itemStatusClass = 'is-completed';
                badgeContent = '✓';  // Checkmark de texto que pintará tu color #10b981
            } else if (isActive) {
                itemStatusClass = 'is-active';
                badgeContent = '📷'; // Emoji de cámara o código para el latido animado
            }

            // El badge de la hora (solo si está completado)
            const badgeTime = isCompleted && point.scannedAt 
                ? `<small>${point.scannedAt}</small>` 
                : '';

            // El botón de acción (solo si es el punto activo)
            const actionButton = isActive 
                ? `
                    <button class="btn-scan" data-checkpoint-id="${point.id}">
                        📷 Escanear Punto
                    </button>
                  ` 
                : '';

            // Retornamos tu estructura HTML exacta mapeada a tu SCSS
            return `
                <div class="timeline-item ${itemStatusClass}">
                    <div class="timeline-badge">${badgeContent}</div>
                    <div class="timeline-card">
                        <div class="timeline-card__body">
                            <div class="timeline-card__header">
                                <h6>${point.name}</h6>
                                ${badgeTime}
                            </div>
                            <p>${point.description || 'Sin descripción disponible.'}</p>
                            ${actionButton}
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }
};


(async function init() {

    const container = qs('.patrol-execution');

    const patrolScheduleExternalId = container.dataset.patrolScheduleExternalId;
    if (!patrolScheduleExternalId) {
        console.error('Atributo "data-patrol-schedule-external-id" ausente.');
        navigateTo('/private/employees/dashboard');
        return;
    }

    bindEvents();

    try {
        let latitude = null;
        let longitude = null;

        if (navigator.geolocation) {
            try {
                const position = await new Promise((resolve, reject) => {
                    navigator.geolocation.getCurrentPosition(resolve, reject, { 
                        timeout: 5000, 
                        enableHighAccuracy: true 
                    });
                });
                latitude = position.coords.latitude;
                longitude = position.coords.longitude;
            } catch (gpsError) {
                console.warn('No se pudo obtener la ubicación, iniciando sin coordenadas:', gpsError);
            }
        }

        const payload = {
            latitude: latitude,
            longitude: longitude,
            clientTimezone: Intl.DateTimeFormat().resolvedOptions().timeZone, // 🟢 Obtiene "America/Santiago" nativamente
            timezoneSource: "CLIENT_BROWSER"
        };

        const response =
                await fetchWithAuth(`/api/patrol-executions/start/${patrolScheduleExternalId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload),
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        currentExecutionData = await response.json();

        renderHeader(container, currentExecutionData);

        renderProgress(container, currentExecutionData);

    } catch (error) {
        console.error('Error al inicializar datos de patrulla activa:', error);
        displayAlert('error', 'Error al sincronizar el estado de la ronda.', 4000);
    }

    console.log('patrol-execute initialized.');

})();