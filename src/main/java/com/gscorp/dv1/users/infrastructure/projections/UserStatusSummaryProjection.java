package com.gscorp.dv1.users.infrastructure.projections;

public interface UserStatusSummaryProjection {
    Long getInvitedUsersCount();
    Long getActiveUsersCount();
    Long getInactiveUsersCount();
    Long getExpiredUsersCount();
    Long getSuspendedUsersCount();
}
