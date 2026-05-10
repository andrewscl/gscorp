import { syncHeaderHeight } from "../../shared/sync-header-height";

document.addEventListener('DOMContentLoaded', () => {
    syncHeaderHeight('.hs-table-header', '--header-height');
});