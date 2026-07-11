package com.gscorp.dv1.hr.employees.infrastructure.Projections;

public interface EmployeeSelectProjection {
    Long getId();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    Long getUserId();
}
