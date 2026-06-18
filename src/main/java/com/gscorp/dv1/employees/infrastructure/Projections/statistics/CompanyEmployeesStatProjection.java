package com.gscorp.dv1.employees.infrastructure.Projections.statistics;

public interface CompanyEmployeesStatProjection {
    String getCompanyName();
    //employeeStatua
    long getHiredCount();
    long getActiveCount();
    long getNoticeGivenCount();
    long getInactiveCount();
    long getSettledCount();
    //userStatus
    long getInvitedUsersCount();
    long getPendingUsersCount();
    long getActiveUsersCount();
    long getInactiveUsersCount();
    long getExpiredUsersCount();
    long getSuspendedUsersCount();

}
