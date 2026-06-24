package com.gscorp.dv1.shiftrequests.web.dto.statistics;

public record ProjectSiteShiftRequestsSummaryDto (
    Long projectId,
    String projectName,
    Long siteId,
    String siteName,
    Long totalShiftsToday
){
    
}
