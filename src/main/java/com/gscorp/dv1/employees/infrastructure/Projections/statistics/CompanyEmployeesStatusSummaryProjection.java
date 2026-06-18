package com.gscorp.dv1.employees.infrastructure.Projections.statistics;

public interface CompanyEmployeesStatusSummaryProjection {
    String getCompanyName();
    long getHiredCount();
    long getActiveCount();
    long getNoticeGivenCount();
    long getInactiveCount();
    long getSettledCount();
}
