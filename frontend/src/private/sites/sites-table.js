import { syncHeaderHeight } from "../../shared/sync-header-height.js";

console.log('[Module] Sites Table loaded');

// Navigation Handler buscará esta función automáticamente
export function init({ container, path }) {
    console.log(`[sites-table] Inicializando para la ruta: ${path}`);
    
    // Llamamos a la lógica de la altura
    syncHeaderHeight('.hs-table-header', '--header-height');
    
}
