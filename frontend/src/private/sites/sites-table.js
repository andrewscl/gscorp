import { syncHeaderHeight } from "../../shared/sync-header-height.js";

document.addEventListener('DOMContentLoaded', () => {
    syncHeaderHeight('.hs-table-header', '--header-height');
});