package com.gscorp.dv1.employees.web.dto.statistics;

import com.gscorp.dv1.employees.infrastructure.Projections.statistics.CompanyEmployeesStatusSummaryProjection;

public record CompanyEmployeesStatusSummaryDto (
    String companyName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){
    public static CompanyEmployeesStatusSummaryDto fromProjection (CompanyEmployeesStatusSummaryProjection p) {
        if (p == null) return null;
        return new CompanyEmployeesStatusSummaryDto(
            p.getCompanyName(),
            p.getHiredCount(),
            p.getActiveCount(),
            p.getNoticeGivenCount(),
            p.getInactiveCount(),
            p.getSettledCount()
        );
    } 
}
