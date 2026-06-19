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
    const companyContainer = qs('#companyStatsContainer');
    const roleContainer = qs('#roleStatsContainer');

    // 1. Renderizado Dinámico de Empresas (Mapeo de Array)
    if (companyContainer) {
        const companiesList = metrics?.companyUsersSummary || [];
        
        if (companiesList.length === 0) {
            companyContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de empresas</p>';
        } else {
            companyContainer.innerHTML = companiesList.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName || 'Empresa sin Nombre'}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${item.activeCount ?? 0} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${item.invitedCount ?? 0} Invitados</small> | <small>🔴 ${item.inactiveCount ?? 0} Inactivos</small>
                    </div>
                </div>
                ${index < companiesList.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }

    // 2. Renderizado Dinámico de Roles (Mapeo de Array)
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