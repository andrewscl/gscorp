package com.gscorp.dv1.employees.web.dto.statistics;

import com.gscorp.dv1.employees.infrastructure.Projections.statistics.CompanyEmployeesUserStatusSummaryProjection;

public record CompanyEmployeesUserStatusSummaryDto (
    String companyName,
    long notInvitedCount,
    long invitedCount,
    long activeCount,
    long inactiveCount,
    long expiredCount,
    long suspendedCount
){

    public static CompanyEmployeesUserStatusSummaryDto
            fromProjection(CompanyEmployeesUserStatusSummaryProjection p) {

        if ( p == null) return null;

        return new CompanyEmployeesUserStatusSummaryDto(
            p.getCompanyName(),
            p.getNotInvitedCount(),
            p.getInvitedCount(),
            p.getActiveCount(),
            p.getInactiveCount(),
            p.getExpiredCount(),
            p.getSuspendedCount()
        );
    }

}
