package com.gscorp.dv1.hr.employees.web.dto.statistics;

import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.ClientEmployeesStatusSummaryProjection;

public record ClientEmployeesStatusSummaryDto (
    String clientName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){
    public static ClientEmployeesStatusSummaryDto fromProjection(ClientEmployeesStatusSummaryProjection p) {
        if (p == null) return null;
        return new ClientEmployeesStatusSummaryDto (
            p.getClientName(),
            p.getHiredCount(),
            p.getActiveCount(),
            p.getNoticeGivenCount(),
            p.getInactiveCount(),
            p.getSettledCount()
        );
    }
}
