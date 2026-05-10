console.log('archivo cargado.');

import { syncHeaderHeight } from "../../shared/sync-header-height.js";

console.log('importación exitosa.');

(function () {
    console.log('syncHeaderHeight ejecución autoimvocada...');
    syncHeaderHeight('.hs-table-header', '--header-height');
})();
