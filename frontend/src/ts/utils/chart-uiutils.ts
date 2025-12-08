/* Utilities to show "no data" state for echarts or DOM container fallback */

export type EChartOrNull = any; // si tienes tipos de echarts, reemplaza `any` por echarts.ECharts

/**
 * Pone el chart en un estado "sin datos". No asume librerías externas salvo la API básica de ECharts.
 * - Si chart es null/undefined no hace nada.
 * - Mensaje aparece centrado dentro del gráfico usando graphic/text; limpia series previas.
 */
export function setNoData(chart: EChartOrNull | null, msg = 'Sin datos') {
  if (!chart) return;

  try {
    chart.clear();

    // opción sencilla que dibuja texto en el centro. Puedes personalizar estilos
    chart.setOption({
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: '45%',
          style: {
            text: msg,
            fontSize: 14,
            fill: '#6b7280', // gris
            fontWeight: 600,
            align: 'center'
          }
        }
      ],
      // desactivar ejes/series visuales
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, { notMerge: true });
  } catch (e) {
    // Silenciar errores si el chart no expone setOption/clear, pero loguear para debug
    // (no arrojar para no romper la UI)
    // eslint-disable-next-line no-console
    console.warn('setNoData: failed to set no-data on chart', e);
  }
}

/**
 * Seguro: intenta aplicar setNoData al chart si existe; si no, renderiza un placeholder DOM
 * dentro del contenedor provisto (container puede ser el elemento padre donde se muestra el gráfico).
 *
 * - chart: instancia de eCharts o null
 * - container: elemento DOM donde el chart vive (puede ser null si nadie lo provee)
 * - msg: texto a mostrar
 */
export function safeSetNoData(chart: EChartOrNull | null, container: Element | null, msg = 'Sin datos') {
  // Preferir chart cuando existe
  if (chart) {
    setNoData(chart, msg);
    return;
  }

  if (!container) return;

  // Try to reuse an existing placeholder
  const existing = container.querySelector('.chart-placeholder');
  if (existing) {
    existing.textContent = msg;
    return;
  }

  // Crear placeholder accesible
  const el = document.createElement('div');
  el.className = 'chart-placeholder';
  el.textContent = msg;
  // estilos mínimos inline; idealmente usa una clase CSS en tu stylesheet
  el.style.display = 'flex';
  el.style.alignItems = 'center';
  el.style.justifyContent = 'center';
  el.style.height = '100%';
  el.style.color = '#6b7280';
  el.style.fontWeight = '600';
  el.style.fontSize = '14px';
  container.appendChild(el);
}