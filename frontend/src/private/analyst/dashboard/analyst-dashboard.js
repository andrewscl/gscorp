import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../signup.js';


const qs  = (s) => document.querySelector(s);

let attendanceMetricList = [];

const loadAttendanceMetricList = async () => {

    try {
        const response =
                await fetchWithAuth('/api/attendance/attendance-dashboard-metrics', {
                    credentials: 'same-origin'
                });

        if (!response.ok) {
            throw new Error(`Error cargando dashboard metrics: ${response.status}`);
        }

        attendanceMetricList = await response.json();
        renderAttendanceDashboardMetrics(attendanceMetricList);

    } catch (e) {
        console.error("No se pudo cargar la lista de attendance metrics:", e);
        attendanceMetricList = [];
    }
}

const renderAttendanceDashboardMetrics = (metrics) => {
    const attendanceContainer = qs('#attendanceStatsContainer');

    if (attendanceContainer) {
        const attendanceList = metrics?.projectSiteAttendancesSummary || [];
        
        if (attendanceList.length === 0) {
            attendanceContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de asistencia</p>';
        } else {

            const groupedByProject = attendanceList.reduce((acc, item) => {
                const projectId = item.projectId || 0;
                if(!acc[projectId]) {
                    acc[projectId] = {
                        projectName: item.projectName || 'Proyecto General',
                        sites: []
                    };
                }
                acc[projectId].sites.push(item);
                return acc; 
            }, {});

            attendanceContainer.innerHTML = Object.values(groupedByProject).map((project, pIndex, pArray) => `
                <div class="project-group mb-3">
                    <div class="project-header d-flex align-items-center gap-2 mb-2">
                        <span class="fs-6 fw-bold text-secondary">📂 ${project.projectName}</span>
                    </div>
                    
                    <div class="project-sites-list ps-3 border-start">
                        ${project.sites.map((site) => `
                            <div class="stat-item py-1">
                                <div class="stat-main-info d-flex justify-content-between align-items-center">
                                    <span class="stat-name text-dark">🏢 ${site.siteName || 'Instalación'}</span>
                                    <span class="badge bg-primary px-3 py-1">${site.attendanceCount ?? 0} Asistencia</span>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
                ${pIndex < pArray.length - 1 ? '<hr class="my-3 opacity-25">' : ''}
            `).join('');

        }
    }
}

