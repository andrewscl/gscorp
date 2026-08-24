package com.gscorp.dv1.operations.shifts.web.dto.statistics;

import java.util.UUID;

import com.gscorp.dv1.operations.shifts.infrastructure.projections.ProjectSiteShiftsSummaryProjection;

public record ProjectSiteShiftsSummaryDto (
    UUID projectExternalId,
    String projectName,
    UUID siteExternalId,
    String siteName,
    Long shifts,
    Long validShifts,
    Long coveredShifts,
    Long uncoveredShifts,
    Double coveredPercentage,
    Double uncoveredPercentage
){
    public static ProjectSiteShiftsSummaryDto fromProjection (
        ProjectSiteShiftsSummaryProjection p
    ){
        if (p == null) return null;

        return new ProjectSiteShiftsSummaryDto(
            p.getProjectExternalId(),
            p.getProjectName(),
            p.getSiteExternalId(),
            p.getSiteName(),
            p.getShifts(),
            p.getValidShifts(),
            p.getCoveredShifts(),
            p.getUncoveredShifts(),
            p.getCoveredPercentage(),
            p.getUncoveredPercentage()
        );

    }
}