import { initHeaderSync } from '../../shared/sync-header-height.js';

console.log(`[sites-table] sites-table activado.`);

export function init({ container, path }) {
    console.log(`[${path}] Inicializando sincronización de cabecera...`);
    
    // Solo una línea de código para que funcione
    initHeaderSync('.hs-table-header', '--header-height');
}