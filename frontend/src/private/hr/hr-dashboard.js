import { fetchWithAuth } from '../../auth.js';
import { navigateTo } from '../../navigation-handler.js';

const qs  = (s) => document.querySelector(s);


let hrMetricList = [];
const loadHrDashboardMetric = async () => {

    try {
        const response =
                await fetchWithAuth('/api/employees/hr-dashboard-metrics', {
                    credentials: 'same-origin'
                });

        if (!response.ok) {
            throw new Error(`Error cargando dashboard metrics: ${response.status}`);
        }

        hrMetricList = await response.json();

        renderHrDashboardMetrics(hrMetricList);

    } catch (e) {

        console.error("No se pudo cargar la lista de metrics:", e);
        hrMetricList = [];
    }
}


const renderHrDashboardMetrics = (metrics) => {
    
    // 1. RENDERIZAR DOTACIÓN POR COMPAÑÍA (Contratos)
    const companyContainer = document.getElementById('companyStatsContainer');
    if (companyContainer && metrics.companyEmployeesStats) { // Ajustado al nombre del DTO unificado
        if (metrics.companyEmployeesStats.length === 0) {
            companyContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de empresas</p>';
        } else {
            companyContainer.innerHTML = metrics.companyEmployeesStats.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName}</strong></span>
                        <span class="stat-badge">${item.stats.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${item.stats.hiredCount} Por Ingresar</small> | <small>⚠️ ${item.stats.noticeGivenCount} En Aviso</small>
                    </div>
                </div>
                ${index < metrics.companyEmployeesStats.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }

    // 2. RENDERIZAR DISTRIBUCIÓN POR CLIENTE
    const clientContainer = document.getElementById('clientStatsContainer');
    if (clientContainer && metrics.clientEmployeesStats) { // Ajustado al nombre del DTO unificado
        if (metrics.clientEmployeesStats.length === 0) {
            clientContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de clientes</p>';
        } else {
            clientContainer.innerHTML = metrics.clientEmployeesStats.map((item, index) => {
                // Sumamos usando la nueva ruta estructurada .stats
                const totalAsignados = item.stats.activeCount + item.stats.hiredCount;
                return `
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${item.clientName}</strong></span>
                            <span class="stat-badge">${totalAsignados} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${item.stats.activeCount} Activos</small> | <small>⏱️ ${item.stats.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${index < metrics.clientEmployeesStats.length - 1 ? '<hr>' : ''}
                `;
            }).join('');
        }
    }

    // 3. RENDERIZAR ADOPCIÓN DIGITAL / ESTADO DE USUARIOS (¡La Card Nueva!)
    const userContainer = document.getElementById('userStatsContainer');
    if (userContainer && metrics.companyEmployeesStats) {
        if (metrics.companyEmployeesStats.length === 0) {
            userContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de usuarios</p>';
        } else {
            userContainer.innerHTML = metrics.companyEmployeesStats.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${item.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${item.stats.invitedUsersCount} Invitados</small> | <small>🔴 ${item.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${item.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${index < metrics.companyEmployeesStats.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }
}

(async function init() {
    console.log("🚀 Inicializando Dashboard de Recursos Humanos...");
    await loadHrDashboardMetric();

})();