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
    
    // 1. Renderizar Bloque de Compañías
    const companyContainer = document.getElementById('companyStatsContainer');
    if (companyContainer && metrics.companyStats) {
        if (metrics.companyStats.length === 0) {
            companyContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de empresas</p>';
        } else {
            companyContainer.innerHTML = metrics.companyStats.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName}</strong></span>
                        <span class="stat-badge">${item.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${item.pendingCount} Por Ingresar</small> | <small>⚠️ ${item.noticeCount} En Aviso</small>
                    </div>
                </div>
                ${index < metrics.companyStats.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }

    // 2. Renderizar Bloque de Clientes
    const clientContainer = document.getElementById('clientStatsContainer');
    if (clientContainer && metrics.clientStats) {
        if (metrics.clientStats.length === 0) {
            clientContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de clientes</p>';
        } else {
            clientContainer.innerHTML = metrics.clientStats.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.clientName}</strong></span>
                        <span class="stat-badge">${item.activeCount + item.pendingCount} Asignados</span>
                    </div>
                    <div class="stat-details">
                        <small>🟢 ${item.activeCount} Activos</small> | <small>⏱️ ${item.pendingCount} Próximos</small>
                    </div>
                </div>
                ${index < metrics.clientStats.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }

    // 3. Renderizar Bloque de Sincronización de Usuarios
    const userContainer = document.getElementById('companyUserStatsContainer');
    if (userContainer && metrics.companyUserStats) {
        if (metrics.companyUserStats.length === 0) {
            userContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de cuentas</p>';
        } else {
            userContainer.innerHTML = metrics.companyUserStats.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName}</strong></span>
                        <span class="stat-badge">${item.totalEmployees} Totales</span>
                    </div>
                    <div class="stat-details mt-1">
                        <span class="text-success">🟢 ${item.activeUsers} Activos</span> | 
                        <span class="text-warning">🟡 ${item.blockedUsers} Bloqueados</span> | 
                        <span class="text-danger">🔴 ${item.withoutUser} Sin Cuenta</span>
                    </div>
                </div>
                ${index < metrics.companyUserStats.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }
};

// --- Ejemplo de integración en el init() de tu fragmento de RRHH ---
/*
async function init() {
    try {
        const response = await fetchWithAuth('/api/hr/dashboard/metrics');
        if (response.ok) {
            const data = await response.json();
            renderHrDashboardMetrics(data);
        }
    } catch (error) {
        console.error("Error cargando métricas de RRHH:", error);
    }
}
*/


(async function init() {
    console.log("🚀 Inicializando Dashboard de Recursos Humanos...");
    await loadHrDashboardMetric();

})();