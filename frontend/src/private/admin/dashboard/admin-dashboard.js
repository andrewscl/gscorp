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

    const userContainer = qs('#userStatsContainer');
    if (userContainer && metrics.usersStatusSummary) {
        if (metrics.usersStatusSummary.length === 0) {
            userContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de usuarios</p>';
        } else {
            userContainer.innerHTML = metrics.usersStatusSummary.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.userStatus}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${item.stats.activeCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${item.usersStatusSummary.invitedCount} Invitados</small> | <small>🔴 ${item.stats.inactiveCount} Inactivos</small> | <small>⏳ ${item.stats.expiredCount} Expirados</small>
                    </div>
                </div>
                ${index < metrics.usersStatusSummary.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }
}


(async function init() {
    console.log("🚀 Inicializando Dashboard de Administración...");
    await loadAdminDashboardMetric();
})();