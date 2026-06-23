package com.gscorp.dv1.attendance.infrastructure.projections.statistics;

public interface ProjectSiteAttendancesSummaryProjection {
    Long getProjectId();
    String getProjectName();
    Long getSiteId();
    String getSiteName();
    Long getAttendances();
}
