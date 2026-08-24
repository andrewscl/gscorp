import { fetchJson } from "../utils/api";
import type { OperationsDashboardResponse } from "../types/ops-dashboard-types";

export const AdminDashboardService = {
    getSiteCoverage: () => 
        fetchJson<OperationsDashboardResponse[]>('/api/admin/admin-dashboard-metrics')
};
