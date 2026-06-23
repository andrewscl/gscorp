package com.gscorp.dv1.attendance.web.dto.statistics;

import com.gscorp.dv1.attendance.infrastructure.projections.statistics.ProjectSiteAttendancesSummaryProjection;

public record ProjectSiteAttendancesSummaryDto (
    Long projectId,
    String projectName,
    Long siteId,
    String siteName,
    Long AttendanceCount
){
    public static ProjectSiteAttendancesSummaryDto fromProjection(ProjectSiteAttendancesSummaryProjection p){
        if (p == null) return null;

        return new ProjectSiteAttendancesSummaryDto(
            p.getProjectId(),
            p.getProjectName(),
            p.getSiteId(),
            p.getSiteName(),
            p.getAttendances()
        );
    }
}
