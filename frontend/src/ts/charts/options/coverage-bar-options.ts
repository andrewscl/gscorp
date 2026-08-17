import { MetricCoverageItem, mapToCoverageDataset } from '../../utils/chart-utils';

export function createCoverageBarOption(
  items: MetricCoverageItem[],
  title: string = 'Cobertura por Sitio',
  subtext: string = 'Turnos cubiertos vs requeridos hoy'
): any {
  return {
    title: {
      text: title,
      subtext: subtext,
      left: 'left',
      textStyle: { color: '#1E293B', fontSize: 15, fontWeight: '600' },
      subtextStyle: { color: '#94A3B8', fontSize: 12 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      appendToBody: true,
      confine: true,
      extraCssText: 'z-index: 99999; box-shadow: 0 8px 18px rgba(0,0,0,0.12); border-radius: 8px;',
      formatter: (params: any) => {
        const row = Array.isArray(params) ? params[0].data : params.data;
        const [cobertura, actual, total, label] = row;

        return `
          <div style="font-weight: 600; color: #1E293B; margin-bottom: 4px;">${label}</div>
          <div style="color: #475569; font-size: 13px;">
            Cobertura: <b style="color: #0F172A;">${cobertura}%</b><br/>
            Guardias: <b style="color: #0F172A;">${actual} / ${total}</b>
          </div>
        `;
      }
    },
    dataset: {
      source: mapToCoverageDataset(items)
    },
    grid: {
      left: '3%',
      right: '8%',
      bottom: '12%',
      top: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%', color: '#64748B' },
      splitLine: { lineStyle: { type: 'dashed', color: '#F1F5F9' } }
    },
    yAxis: {
      type: 'category',
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#CBD5E1' } },
      axisLabel: { color: '#334155', fontWeight: '500' }
    },
    visualMap: {
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      min: 0,
      max: 100,
      text: ['100%', '0%'],
      dimension: 0,
      inRange: {
        color: ['#EF4444', '#F59E0B', '#10B981']
      },
      textStyle: { color: '#64748B', fontSize: 11 }
    },
    series: [
      {
        type: 'bar',
        barWidth: '50%',
        itemStyle: { borderRadius: [0, 6, 6, 0] },
        label: {
          show: true,
          position: 'right',
          formatter: (params: any) => `${params.value[0]}%`,
          fontWeight: 'bold',
          color: '#475569'
        },
        encode: { x: 'cobertura', y: 'sitio' }
      }
    ]
  };
}