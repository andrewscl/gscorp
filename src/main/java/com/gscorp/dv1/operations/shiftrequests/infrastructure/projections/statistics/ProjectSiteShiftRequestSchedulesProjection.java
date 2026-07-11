package com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.statistics;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gscorp.dv1.enums.DayOfWeek;

public interface ProjectSiteShiftRequestSchedulesProjection {
    Long getId();
    Long getShiftRequestId();
    DayOfWeek getDayFrom();
    DayOfWeek getDayTo();
    LocalTime getStartTime();
    LocalTime getEndTime();
    LocalDate getRequestStartDate();
    LocalDate getRequestEndDate();
    Long getProjectId();
    String getProjectName();
    Long getSiteId();
    String getSiteName();
}
