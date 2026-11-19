import { safeSetNoData } from '../../utils/chart-uiutils';
import type { ChartController } from '../../types/chart-types';
import type { ShiftsCountLast24HoursDto } from '../../types/ops-dashboard-types';
import type { EChartsType } from 'echarts/core';

export async function initShiftsCoverageDonut(
  selector: string,
  options: { mkChart: (el: HTMLElement) => EChartsType | null }
): Promise<ChartController<ShiftsCountLast24HoursDto[]> | null> {

  const element = document.querySelector<HTMLElement>(selector);
  if (!element) return null;
  const instance = options.mkChart(element);
  if (!instance) return null;

  const container: HTMLElement = element;
  const chart: EChartsType = instance;

  function render(dataList?: ShiftsCountLast24HoursDto[]) {
    chart.clear();
    container.querySelector('.chart-placeholder')?.remove();

    if (!dataList?.length) {
      safeSetNoData(chart, container, 'Sin datos de cobertura');
      return;
    }

    const covered = dataList.reduce((sum, item) => sum + item.coveredShifts, 0);
    const total = dataList.reduce((sum, item) => sum + item.validShifts, 0);

    if (!Number.isFinite(covered) || !Number.isFinite(total)
        || covered < 0 || total < 0 || covered > total) {
      console.error('Datos de cobertura inválidos', dataList);
      safeSetNoData(chart, container, 'Error en datos de cobertura');
      return;
    }

    if (total === 0) {
      safeSetNoData(chart, container, 'Sin turnos válidos');
      return;
    }

    const uncovered = total - covered;
    const percentage = (covered / total * 100).toFixed(1);

    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        name: 'Cobertura',
        type: 'pie',
        radius: ['62%', '82%'],
        label: {
          show: true,
          position: 'center',
          formatter: `${percentage}%\n${covered} / ${total}`,
          fontSize: 16,
          fontWeight: 700
        },
        labelLine: { show: false },
        data: [
          { value: covered, name: 'Cubiertos', itemStyle: { color: '#10B981' } },
          { value: uncovered, name: 'Sin cubrir', itemStyle: { color: '#E5E7EB' } }
        ]
      }]
    });
  }

  return {
    render,
    destroy() {
      chart.dispose();
      container.querySelector('.chart-placeholder')?.remove();
    },
    resize: () => chart.resize(),
    chart,
    container
  };
}