import { EChartsInstance } from "../lib/echarts-setup";

/**
 * Contrato estricto para los controladores de gráficos.
 * Utilizado por los dashboards para orquestar el renderizado,
 * resize y limpieza de memoria sin usar 'any'.
 */
export interface ChartController <TData = any>{
  /**
   * Método de renderizado. 
   * Acepta data opcional para patrones Dumb/Orquestador, 
   * o sin argumentos para componentes autónomos.
   */
  render: (data?: TData) => Promise<void> | void;
  /**
   * Métodos opcionales de ciclo de vida
   */
  destroy?: () => void;
  stop?: () => void;
  /**
   * Referencias directas al motor gráfico y al contenedor DOM
   */
  chart?: EChartsInstance | null;
  container?: HTMLElement | null;
}
