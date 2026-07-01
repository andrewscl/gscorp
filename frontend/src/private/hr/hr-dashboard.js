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
            throw
                new Error(`Error cargando dashboard metrics: ${response.status}`);
        }

        hrMetricList = await response.json();

        renderHrDashboardMetrics(hrMetricList);
        updateHeaderCards(hrMetricList);

    } catch (e) {

        console.error("No se pudo cargar la lista de metrics:", e);
        hrMetricList = [];
    }
}


function updateHeaderCards(metrics) {
    const holding = metrics.employeeStatusSummary[0];

    if (!holding) return;

    qs('#kpi-hired').innerText = holding.hiredCount;
    qs('#kpi-active').innerText = holding.activeCount;
    qs('#kpi-notice').innerText = holding.noticeGivenCount;
    qs('#kpi-inactive').innerText = holding.inactiveCount;
    qs('#kpi-settled').innerText = holding.settledCount;
}


const renderHrDashboardMetrics = (metrics) => {
    
    // 1. RENDERIZAR DOTACIÓN POR COMPAÑÍA (Contratos)
    const companyContainer = qs('#companyStatsContainer');
    if (companyContainer && metrics.companyEmployeesStatusSummary) { // Ajustado al nombre del DTO unificado
        if (metrics.companyEmployeesStatusSummary.length === 0) {
            companyContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de empresas</p>';
        } else {
            companyContainer.innerHTML = metrics.companyEmployeesStatusSummary.map((item, index) => `
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${item.companyName}</strong></span>
                        <span class="stat-badge">${item.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${item.hiredCount} Por Ingresar</small> | <small>⚠️ ${item.noticegivenCount} En Aviso</small>
                    </div>
                </div>
                ${index < metrics.companyEmployeesStatusSummary.length - 1 ? '<hr>' : ''}
            `).join('');
        }
    }

    // 2. RENDERIZAR DISTRIBUCIÓN POR CLIENTE
    const clientContainer = qs('#clientStatsContainer');
    if (clientContainer && metrics.companyEmployeesStatusSummary) { // Ajustado al nombre del DTO unificado
        if (metrics.companyEmployeesStatusSummary.length === 0) {
            clientContainer.innerHTML = '<p class="text-muted text-center py-2">No hay datos de clientes</p>';
        } else {
            clientContainer.innerHTML = metrics.companyEmployeesStatusSummary.map((item, index) => {
                // Sumamos usando la nueva ruta estructurada .stats
                const totalAsignados = item.activeCount + item.hiredCount + item.noticegivenCount;
                return `
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${item.clientName}</strong></span>
                            <span class="stat-badge">${totalAsignados} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${item.activeCount} Activos</small> | <small>⏱️ ${item.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${index < metrics.companyEmployeesStatusSummary.length - 1 ? '<hr>' : ''}
                `;
            }).join('');
        }
    }


    const company


}


(async function init() {
    console.log("🚀 Inicializando Dashboard de Recursos Humanos...");
    await loadHrDashboardMetric();
})();