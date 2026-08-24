import { fetchJson } from "../utils/api";
import type { OperationsDashboardResponse } from "../types/ops-dashboard-types";

export class OpsDashboardService {
    async getOpsDashboardMetrics(zoneIdStr?: string): Promise<OperationsDashboardResponse>{
        const params = new URLSearchParams();
        if(zoneIdStr){
            params.append('zoneIdStr', zoneIdStr);
        }
        const queryString = params.toString();
        const url = queryString
            ? `/api/operations/ops-dashboard-metrics?${queryString}` 
            : '/api/operations/ops-dashboard-metrics';
        return fetchJson<OperationsDashboardResponse>(url);
    }
}
