package com.gscorp.dv1.employees.web.dto.statistics;

import com.gscorp.dv1.employees.infrastructure.Projections.statistics.CompanyEmployeesStatProjection;

public record CompanyEmployeesStatDto (
    String companyName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){
    public static CompanyEmployeesStatDto fromProjection (CompanyEmployeesStatProjection p) {
        if (p == null) return null;
        return new CompanyEmployeesStatDto(
            p.getCompanyName(),
            p.getHiredCount(),
            p.getActiveCount(),
            p.getNoticeGivenCount(),
            p.getInactiveCount(),
            p.getSettledCount()
        );
    } 
}
