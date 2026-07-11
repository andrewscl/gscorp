package com.gscorp.dv1.hr.employees.web.dto.statistics;

import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.EmployeesStatusSummaryProjection;

public record EmployeesStatusSummaryDto (
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){
    public static EmployeesStatusSummaryDto fromProjection(EmployeesStatusSummaryProjection p) {
        if (p == null) return null;
        return new EmployeesStatusSummaryDto (
            p.getHiredCount(),
            p.getActiveCount(),
            p.getNoticeGivenCount(),
            p.getInactiveCount(),
            p.getSettledCount()
        );
    }
}
