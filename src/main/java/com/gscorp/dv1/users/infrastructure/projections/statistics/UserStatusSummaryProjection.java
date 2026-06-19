package com.gscorp.dv1.users.infrastructure.projections.statistics;

public interface UserStatusSummaryProjection {
    Long getInvitedUsersCount();
    Long getActiveUsersCount();
    Long getInactiveUsersCount();
    Long getExpiredUsersCount();
    Long getSuspendedUsersCount();
}
