package com.gscorp.dv1.operations.shiftrequests.web.dto.statistics;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.statistics.ProjectSiteShiftRequestSchedulesProjection;

public record ProjectSiteShiftRequestsSchedulesDto (
    Long id,
    Long shiftRequestId,
    DayOfWeek dayFrom,
    DayOfWeek dayTo,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate requestStartDate,
    LocalDate requestEndDate,
    Long projectId,
    String projectName,
    Long siteId,
    String siteName
){
    public static ProjectSiteShiftRequestsSchedulesDto
                        fromProjection (ProjectSiteShiftRequestSchedulesProjection p){
        if (p == null) return null;

        return new ProjectSiteShiftRequestsSchedulesDto(
            p.getId(),
            p.getShiftRequestId(),
            p.getDayFrom(),
            p.getDayTo(),
            p.getStartTime(),
            p.getEndTime(),
            p.getRequestStartDate(),
            p.getRequestEndDate(),
            p.getProjectId(),
            p.getProjectName(),
            p.getSiteId(),
            p.getSiteName()
        );
    }
}
