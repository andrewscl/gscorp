package com.gscorp.dv1.users.web.dto.statistics;

import com.gscorp.dv1.users.infrastructure.projections.UserStatusSummaryProjection;

public record UserStatusSummaryDto (
    long invitedCount,
    long activeCount,
    long inactiveCount,
    long expiredCount,
    long suspendedCount
){
    public static UserStatusSummaryDto fromProjection(UserStatusSummaryProjection p){
        if (p == null) return null;

        return new UserStatusSummaryDto(
            p.getInvitedUsersCount(),
            p.getActiveUsersCount(),
            p.getInactiveUsersCount(),
            p.getExpiredUsersCount(),
            p.getSuspendedUsersCount()
        );
    }
}
