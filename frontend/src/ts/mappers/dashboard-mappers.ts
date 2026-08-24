import type { SitesCoverageDto } from "../types/api-types";
import type { MetricCoverageItem } from "../utils/chart-utils";


export function mapSiteCoverageDtoToMetricCoverage(dto: SitesCoverageDto)
                                                            : MetricCoverageItem {
        return {
            label: dto.siteName,
            actual: dto.coveredShifts,
            total: dto.requiredShifts,
            percentage: dto.coveragePercentage
        };
    }