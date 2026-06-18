package com.gscorp.dv1.users.infrastructure.projections;

public interface UserStatusSummaryProjection {
    
    long getInvitedUsersCount();
    long getPendingUsersCount();
    long getActiveUsersCount();
    long getInactiveUsersCount();
    long getExpiredUsersCount();
    long getSuspendedUsersCount();

}
