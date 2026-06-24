package com.gscorp.dv1.shiftrequests.infrastructure.projections.statistics;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ProjectSiteShiftRequestSchedulesProjection {
    Long getId();
    Long getShiftRequestId();
    String getDayFrom();
    String getDayTo();
    LocalTime getStartTime();
    LocalTime getEndTime();
    LocalDate getRequestStartDate();
    LocalDate getRequestEndDate();
    Long getProjectId();
    String getProjectName();
    Long getSiteId();
    String getSiteName();
}
