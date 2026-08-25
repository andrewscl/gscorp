import type { ProjectSiteShiftsSummaryDto } from "../types/ops-dashboard-types";
import type { MetricCoverageItem } from "../utils/chart-utils";


export function mapSiteCoverageDtoToMetricCoverage(dto: ProjectSiteShiftsSummaryDto)
                                                            : MetricCoverageItem {
        return {
            label: dto.siteName,
            actual: dto.coveredShifts,
            total: dto.shifts,
            percentage: dto.coveredPercentage
        };
}