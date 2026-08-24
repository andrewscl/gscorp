package com.gscorp.dv1.operations.shifts.infrastructure.projections;

import java.util.UUID;

public interface ProjectSiteShiftsSummaryProjection {
    UUID getProjectExternalId();
    String getProjectName();
    UUID getSiteExternalId();
    String getSiteName();
    Long getShifts();
    Long getValidShifts();
    Long getCoveredShifts();
    Long getUncoveredShifts();
    Double getCoveredPercentage();
    Double getUncoveredPercentage();
}
