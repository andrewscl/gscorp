import { syncHeaderHeight } from "../../shared/sync-header-height";

document.addEventListener('DOMContentLoaded', () => {
    syncHeaderHeight('.header', '--header-height');
});