import { fetchWithAuth } from '../../../auth.js';
import { navigateTo } from '../../../signup.js';


const qs  = (s) => document.querySelector(s);


let adminMetricList = [];
const loadAdminDashboardMetric = async () => {

    try {
        const response =
                await fetchWithAuth('/api/users/admin-dashboard-metrics', {
                    credentials: 'same-origin'
                });

        if (!response.ok) {
            throw new Error(`Error cargando dashboard metrics: ${response.status}`);
        }

        adminMetricList = await response.json();

        renderAdminDashboardMetrics(adminMetricList);
        updateHeaderCards(adminMetricList);

    } catch (e) {

        console.error("No se pudo cargar la lista de metrics:", e);
        adminMetricList = [];
    }
}


function updateHeaderCards(metrics) {
    const holding = metrics.usersStatusSummary[0];
    if (!holding) return;

    qs('#kpi-invited').innerText = holding.invitedCount;
    qs('#kpi-active').innerText = holding.activeCount;
    qs('#kpi-inactive').innerText = holding.inactiveCount;
    qs('#kpi-expired').innerText = holding.expiredCount;
    qs('#kpi-suspended').innerText = holding.suspendedCount;
}


const renderAdminDashboardMetrics = (metrics) => {
    const roleContainer = qs('#roleStatsContainer');

    if (roleContainer) {
        const rolesList = metrics?.roleUsersSummary || [];
        
        if (rolesList.length === 0) {
            roleContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de roles</p>';
        } else {
            roleContainer.innerHTML = rolesList.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name">💼 <strong>${item.roleName || 'Rol del Sistema'}</strong></span>
                        <span class="badge bg-primary px-3 py-1">${item.totalUsers ?? 0} Usuarios</span>
                    </div>
                </div>
                ${index < rolesList.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }
}


(async function init() {
    console.log("🚀 Inicializando Dashboard de Administración...");
    await loadAdminDashboardMetric();
})();