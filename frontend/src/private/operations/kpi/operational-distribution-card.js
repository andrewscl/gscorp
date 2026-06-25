import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../signup.js';


const qs  = (s) => document.querySelector(s);

let metricList = [];


const loadOperationsMetricList = async () => {

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
    opsDashboardContainer.innerHTML = Object.values(groupedByProject).map((project, pIndex, pArray) => `
    <div class="project-group">
            <div class="project-header">
                📂 ${project.projectName}
            </div>
            
            <div class="project-sites-list">
                ${Object.values(project.sites).map((site) => {
                    const hasPending = site.totalShiftsToday > 0;
                    return `
                        <div class="stat-item-row">
                            <span class="site-cell-name">🏢 ${site.siteName}</span>
                            
                            <span class="metric-cell-val text-primary fw-semibold">
                                ${site.attendanceCount}
                            </span>
                            
                            <span class="metric-cell-val text-center">
                                <span class="pill-value ${hasPending ? 'pill-pending' : 'pill-ok'}">
                                    ${site.totalShiftsToday}
                                </span>
                            </span>
                        </div>
                    `;
                }).join('')}
            </div>
        </div>
        ${pIndex < pArray.length - 1 ? '<hr>' : ''}
    `).join('');

}


(async function init() {
    console.log("🚀 Inicializando Dashboard de Operaciones...");
    await loadOperationsMetricList();
})();