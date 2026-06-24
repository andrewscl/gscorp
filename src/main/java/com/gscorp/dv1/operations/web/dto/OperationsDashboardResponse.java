package com.gscorp.dv1.operations.web.dto;

import java.util.List;

import com.gscorp.dv1.attendance.web.dto.statistics.ProjectSiteAttendancesSummaryDto;
import com.gscorp.dv1.shiftrequests.web.dto.statistics.ProjectSiteShiftRequestsSummaryDto;

public record OperationsDashboardResponse (

    List<ProjectSiteShiftRequestsSummaryDto> projectSiteShiftsSummary,
    List<ProjectSiteAttendancesSummaryDto> projectSiteAttendancesSummary

){}
