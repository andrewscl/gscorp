package com.gscorp.dv1.employees.infrastructure.Projections.statistics;

public interface ClientEmployeesStatProjection {
    String getClientName();
    long getHiredCount();
    long getActiveCount();
    long getNoticeGivenCount();
    long getInactiveCount();
    long getSettledCount();
    //userStatus
    long getInvitedUsersCount();
    long getActiveUsersCount();
    long getInactiveUsersCount();
    long getExpiredUsersCount();
    long getSuspendedUsersCount();

}
