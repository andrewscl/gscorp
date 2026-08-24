import { mkChart } from "../../lib/echarts-setup";
import { AdminDashboardService } from "../../services/admin-dashboard-service";
import { mapSiteCoverageDtoToMetricCoverage } from "../../mappers/dashboard-mappers";
import { createCoverageBarOption } from "../options/coverage-bar-options";
import type { ChartController } from "../../types/chart-types";

export async function initSiteCoverageBarChart(
    selector: string
): Promise<ChartController | null> {
    // 1. Obtener elemento del DOM.
    const container = document.querySelector(selector) as HTMLElement;
    if(!container) return null;
    // 2. Crear o reutilizar la instancia Echarts.
    const chart = mkChart(container);
    if(!chart) return null;
    // 3. Definir la función render interna.
    const render = async () => {
        try {
            chart?.showLoading({ text: 'Cargando datos...' });

            const dtos = await AdminDashboardService.getSiteCoverage();
            const metrics = dtos.map(mapSiteCoverageDtoToMetricCoverage);
            const option = createCoverageBarOption(metrics);

            chart?.hideLoading();
            chart?.setOption(option);
        } catch (error) {
            chart?.hideLoading();
            console.error(`[SiteCoverageChart] Error renderizando ${selector}:`, error);
        }
    };
    // 4. Retornar el contrato ChartController para el dashboard
    return {
        render,
        chart,
        container,
        destroy: () => chart?.dispose()
    };
}