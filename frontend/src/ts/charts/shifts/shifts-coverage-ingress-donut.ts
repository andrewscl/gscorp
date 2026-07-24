import { mkChart, echarts } from '../../lib/echarts-setup';
import { fetchWithTimeout } from '../../utils/api';
import { safeSetNoData } from '../../utils/chart-uiutils';

type ChartController = {
  render?: () => Promise<void> | void;
  destroy?: () => void;
  chart?: any;
  container?: HTMLElement | null;
};

interface HourlyCoverageData {
  startTs: string; // Ej: "2026-07-23T08:00:00Z" o directamente "08:00"
  totalShifts: number;
  actualShifts: number;
}

export async function initShiftCoverageDonuts(
  selector: string,
  options: { mkChart: typeof mkChart; fetchWithTimeout: typeof fetchWithTimeout }
): Promise<ChartController> {
  
  const gridContainer = document.querySelector(selector) as HTMLElement | null;
  const activeCharts = new Map<string, echarts.ECharts>();

  async function render() {
    if (!gridContainer) return;

    // Buscar el template inerte en el documento
    const template = document.getElementById('tmpl-hourly-donut-card') as HTMLTemplateElement | null;
    if (!template) {
      console.error('No se encontró el template #tmpl-hourly-donut-card');
      return;
    }

    try {
      const response = await options.fetchWithTimeout(
        '/api/shifts/last-24hours-shifts', 
        { method: 'GET' }, 
        15000, 
        true
      );

      if (!response.ok) throw new Error('HTTP Error');
      const dataList: HourlyCoverageData[] = await response.json();

      // 1. Limpieza total de instancias de ECharts previas para evitar fugas de memoria
      activeCharts.forEach(chart => chart.dispose());
      activeCharts.clear();

      // 2. Limpieza segura del DOM dinámico anterior
      const dynamicCards = gridContainer.querySelectorAll('.card[data-card="hourly-shift-node"]');
      dynamicCards.forEach(card => card.remove());

      // 3. Mapear y renderizar dinámicamente cada bloque horario del backend
      dataList.forEach((item, index) => {
        const clone = template.content.cloneNode(true) as DocumentFragment;
        
        // Selectores internos del fragmento según tu HTML
        const titleNode = clone.querySelector('.block-title') as HTMLElement;
        const valueNode = clone.querySelector('.total-value') as HTMLElement;
        const metaNode = clone.querySelector('.meta-value') as HTMLElement;
        const donutCanvasNode = clone.querySelector('.hourly-donut') as HTMLElement;

        // Procesar la hora: si viene en formato ISO string, la convertimos. Si ya viene limpia, la usamos directo.
        let hoursStr = item.startTs;
        if (item.startTs && (item.startTs.includes('T') || !isNaN(Date.parse(item.startTs)))) {
            const date = new Date(item.startTs);
            hoursStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
        }
        
        if (titleNode) titleNode.textContent = `${hoursStr} hrs`;
        
        // Mostramos el valor real actual (o los pendientes si cambias la lógica)
        // if (valueNode) valueNode.textContent = item.actualShifts.toString();
        if (valueNode) valueNode.textContent = 'dos';
        if (metaNode) {
            metaNode.textContent = item.totalShifts.toString();
        }
        
        // Asignar ID único para inicializar ECharts sin colisiones
        const chartUniqueId = `donut-chart-node-${index}`;
        if (donutCanvasNode) {
            donutCanvasNode.id = chartUniqueId;
        }

        // Inyectamos la tarjeta al DOM real
        gridContainer.appendChild(clone);

        // 4. Inicializar ECharts sobre el nodo inyectado ya presente en el DOM
        const elDonut = document.getElementById(chartUniqueId);
        if (!elDonut) return;

        const cs = getComputedStyle(elDonut);
        if (elDonut.offsetWidth === 0 && (!cs.width || cs.width === '0px')) {
            elDonut.style.width = '100%';    // Ajusta al contenedor
            elDonut.style.height = '110px';  // 👈 Asigna el alto que necesite tu diseño
        }

        const chart = options.mkChart(elDonut);
        if (!chart) return;
        
        // Guardamos la instancia en el mapa
        activeCharts.set(chartUniqueId, chart);

        // Lógica matemática del Donut corregida con tus propiedades
        const hasMeta = item.totalShifts > 0;
        const percentage = hasMeta ? Math.round((2 / item.totalShifts) * 100) : 0;
        const pctForSeries = hasMeta ? Math.min(100, Math.max(0, percentage)) : 100;

        chart.setOption({
          tooltip: {
            trigger: 'item',
            appendToBody: true,
            confine: true,
            extraCssText: 'z-index: 99999; box-shadow: 0 8px 18px rgba(0,0,0,0.12);',
            position: () => {
              const r = elDonut.getBoundingClientRect();
              return [Math.round(r.left + r.width / 2), Math.round(r.top + r.height / 2) - 12];
            },
            formatter: (p: any) => {
              const data = Array.isArray(p) ? p[0] : p;
              if (!hasMeta) return 'Meta no definida';
              if (data.name === 'Cumplido') {
                return `${data.marker || ''} Cubiertos: ${2} / ${item.totalShifts} (${percentage}%)`;
              }
              return `${data.marker || ''} Vacantes: ${Math.max(0, item.totalShifts - 2)}`;
            }
          },
          series: [{
            name: 'Cobertura',
            type: 'pie',
            radius: ['62%', '82%'],
            avoidLabelOverlap: false,
            emphasis: { scale: false },
            label: {
              show: true,
              position: 'center',
              formatter: hasMeta ? `${percentage}%` : '—',
              fontSize: 15,
              fontWeight: 700,
              color: '#485572'
            },
            labelLine: { show: false },
            data: !hasMeta
              ? [{ value: 1, name: 'Sin meta', itemStyle: { color: '#E5E7EB' } }]
              : [
                  { value: pctForSeries, name: 'Cumplido', itemStyle: { color: '#10B981' } },
                  { value: 100 - pctForSeries, name: 'Pendiente', itemStyle: { color: '#E5E7EB' } }
                ]
          }]
        });
        // 🌟 OBLIGAR A RECALCULAR: Si se inicializó en 0px, esto forzará el pintado real
        try { chart.resize(); } catch (_) {}
      });

    } catch (error) {
      console.error('Error renderizando donuts de cobertura:', error);
      gridContainer.innerHTML = '';
      safeSetNoData(null, gridContainer, 'Error al cargar cobertura');
    }
  }

  function destroy() {
    activeCharts.forEach(chart => chart.dispose());
    activeCharts.clear();
    if (gridContainer) {
      const dynamicCards = gridContainer.querySelectorAll('.card[data-card="hourly-shift-node"]');
      dynamicCards.forEach(card => card.remove());
    }
  }

  return {
    render,
    destroy,
    container: gridContainer,
    chart: {
      resize: () => activeCharts.forEach(chart => chart.resize())
    }
  };
}