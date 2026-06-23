package com.gscorp.dv1.attendance.web.dto.statistics;

import java.util.List;

public record AttendanceDistributionMetricResponse (
    List<ProjectSiteAttendancesSummaryDto> projectSiteAttendancesSummary
){
     
}
