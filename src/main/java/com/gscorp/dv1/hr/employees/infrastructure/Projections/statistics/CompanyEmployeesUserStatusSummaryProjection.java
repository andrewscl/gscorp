package com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics;

public interface CompanyEmployeesUserStatusSummaryProjection {
    String getCompanyName();
    long getNotInvitedCount();
    long getInvitedCount();
    long getActiveCount();
    long getInactiveCount();
    long getExpiredCount();
    long getSuspendedCount();
}
