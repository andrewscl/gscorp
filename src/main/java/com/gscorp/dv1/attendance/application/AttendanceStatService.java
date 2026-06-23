package com.gscorp.dv1.attendance.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.attendance.web.dto.statistics.ProjectSiteAttendancesSummaryDto;

public interface AttendanceStatService {

    List<ProjectSiteAttendancesSummaryDto>
                getProjectSiteAttendancesTodaySummaryByUserExternalId(UUID userExternalId);
    
}


