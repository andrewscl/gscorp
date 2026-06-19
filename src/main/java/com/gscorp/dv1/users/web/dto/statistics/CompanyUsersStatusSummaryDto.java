package com.gscorp.dv1.users.web.dto.statistics;

import com.gscorp.dv1.users.infrastructure.projections.statistics.CompanyUsersStatusSummaryProjection;

public record CompanyUsersStatusSummaryDto (
    String companyName,
    Long invitedCount,
    Long activeCount,
    Long inactiveCount,
    Long expiredCount,
    Long suspendedCount
){
    public static CompanyUsersStatusSummaryDto fromProjection(CompanyUsersStatusSummaryProjection p){
        if (p == null) return null;

        return new CompanyUsersStatusSummaryDto(
            p.getCompanyName(),
            p.getInvitedUsersCount(),
            p.getActiveUsersCount(),
            p.getInactiveUsersCount(),
            p.getExpiredUsersCount(),
            p.getSuspendedUsersCount()
        );
    }
}
