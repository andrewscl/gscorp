package com.gscorp.dv1.employees.infrastructure.Projections.statistics;

public interface CompanyEmployeesStatProjection {
    String getCompanyName();
    long getHiredCount();
    long getActiveCount();
    long getNoticeGivenCount();
    long getInactiveCount();
    long getSettledCount();
}
