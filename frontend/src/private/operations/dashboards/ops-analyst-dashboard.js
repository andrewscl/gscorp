import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../signup.js';


const qs  = (s) => document.querySelector(s);

let attendanceMetricList = [];


const loadAttendanceMetricList = async () => {

    try {
        const response =
                await fetchWithAuth('/api/operations/ops-dashboard-metrics', {
                    credentials: 'same-origin'
                });

        if (!response.ok) {
            throw new Error(`Error cargando dashboard metrics: ${response.status}`);
        }

        metricList = await response.json();
        renderAttendanceDashboardMetrics(metricList);

    } catch (e) {
        console.error("No se pudo cargar la lista de attendance metrics:", e);
        metricList = [];
    }
}


const renderAttendanceDashboardMetrics = (metrics) => {
    const opsDashboardContainer = qs('#operationStatsContainer');
    if (!opsDashboardContainer) return;

    // 1. Extraer de la respuesta única de tu RestController ambas listas
    const shiftRequestsList = metrics?.projectSiteShiftsSummary || [];
    const attendanceList = metrics?.projectSiteAttendancesSummary || [];

    if (shiftRequestsList.length === 0 && attendanceList.length === 0) {
            opsDashboardContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de asistencia</p>';
            return;
    }

    // 2. Estructura de agrupación unificada    
    const groupedByProject = {};

    // Auxiliar para inicializar nodos y evitar duplicaciones
    const getOrCreateSite = (projectId, projectName, siteId, siteName) => {
        if (!groupedByProject[projectId]) {
            groupedByProject[projectId] = { projectName: projectName || 'Proyecto General', sites: {} };
        }
        if (!groupedByProject[projectId].sites[siteId]) {
            groupedByProject[projectId].sites[siteId] = {
                siteName: siteName || 'Instalación',
                attendanceCount: 0,
                pendingShiftsCount: 0
            };
        }
        return groupedByProject[projectId].sites[siteId];
    };

    // 3. Mapear Asistencias al acumulador
    attendanceList.forEach(item => {
        const site = getOrCreateSite(item.projectId || 0, item.projectName, item.siteId || 0, item.siteName);
        site.attendanceCount = item.attendanceCount ?? 0;
    });

    // 4. Mapear Solicitudes de Turnos (ShiftRequests) al acumulador
    shiftRequestsList.forEach(item => {
        const site = getOrCreateSite(item.projectId || 0, item.projectName, item.siteId || 0, item.siteName);
        site.pendingShiftsCount = item.totalShiftsToday ?? 0; // Ajusta el campo según tu query/DTO de turnos
    });

    // 5. Renderizar el HTML Dinámico con diseño de dos columnas (badges alineados)
    attendanceContainer.innerHTML = Object.values(groupedByProject).map((project, pIndex, pArray) => `
        <div class="project-group mb-3">
            <div class="project-header d-flex align-items-center gap-2 mb-2">
                <span class="fs-6 fw-bold text-secondary">📂 ${project.projectName}</span>
            </div>
            
            <div class="project-sites-list ps-3 border-start">
                ${Object.values(project.sites).map((site) => `
                    <div class="stat-item py-2 border-bottom border-light">
                        <div class="stat-main-info d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <span class="stat-name text-dark fw-medium">🏢 ${site.siteName}</span>
                            
                            <div class="d-flex gap-2">
                                <span class="badge bg-light text-primary border border-primary-subtle px-2 py-1">
                                    👤 ${site.attendanceCount} Asistencias
                                </span>
                                <span class="badge ${site.pendingShiftsCount > 0 ? 'bg-danger-subtle text-danger' : 'bg-light text-muted'} px-2 py-1">
                                    🚨 ${site.pendingShiftsCount} Por Cubrir
                                </span>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
        ${pIndex < pArray.length - 1 ? '<hr class="my-3 opacity-25">' : ''}
    `).join('');

}


(async function init() {
    console.log("🚀 Inicializando Dashboard de Operaciones...");
    await loadAttendanceMetricList();
})();