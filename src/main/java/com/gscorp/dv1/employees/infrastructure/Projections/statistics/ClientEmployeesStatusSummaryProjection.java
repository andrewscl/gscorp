package com.gscorp.dv1.employees.infrastructure.Projections.statistics;

public interface ClientEmployeesStatusSummaryProjection {
    String getClientName();
    long getHiredCount();
    long getActiveCount();
    long getNoticeGivenCount();
    long getInactiveCount();
    long getSettledCount();
}
