import { mkChart } from "../../lib/echarts-setup";
import { mapSiteCoverageDtoToMetricCoverage } from "../../mappers/dashboard-mappers";
import { createCoverageBarOption } from "../options/coverage-bar-options";
import type { ChartController } from "../../types/chart-types";
import type { ProjectSiteShiftsSummaryDto } from "../../types/ops-dashboard-types";

export async function initSiteCoverageBarChart(
    selector: string
): Promise<ChartController<ProjectSiteShiftsSummaryDto[]> | null> {
    // 1. Obtener elemento del DOM.
    const container = document.querySelector(selector) as HTMLElement;
    if(!container) return null;
    // 2. Crear o reutilizar la instancia Echarts.
    const chart = mkChart(container);
    if(!chart) return null;
    // 3. Definir la función render interna.
    const render = (data?: ProjectSiteShiftsSummaryDto[]) => {
        console.log('[SiteCoverageChart] Datos recibidos en render:', data);
        try {
            if (!data) {
                console.warn('[SiteCoverageChart] El arreglo viene undefined o vacío');
                return;
            }
            // Transformación DTO a las opciones del grafico.
            const metrics = data.map(mapSiteCoverageDtoToMetricCoverage);
            const option = createCoverageBarOption(metrics);
            chart?.hideLoading();
            chart?.setOption(option, true);
        } catch (error) {
            chart?.hideLoading();
            console.error(`[SiteCoverageChart] Error renderizando ${selector}:`, error);
        }
    };
    // Mostrar spinner inicial hasta que el orquestador envíe la data
    chart.showLoading({ text: 'Cargando datos...' });
    // 4. Retornar el contrato ChartController para el dashboard
    return {
        render,
        chart,
        container,
        destroy: () => chart?.dispose(),
        resize: () => chart?.resize()
    };
}