package com.gscorp.dv1.users.infrastructure.projections.statistics;

public interface CompanyUsersStatusSummaryProjection {
    String getCompanyName();
    Long getInvitedUsersCount();
    Long getActiveUsersCount();
    Long getInactiveUsersCount();
    Long getExpiredUsersCount();
    Long getSuspendedUsersCount();
}
