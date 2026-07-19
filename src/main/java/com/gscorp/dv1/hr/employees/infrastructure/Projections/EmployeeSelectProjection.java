package com.gscorp.dv1.hr.employees.infrastructure.Projections;

import java.util.UUID;

public interface EmployeeSelectProjection {
    Long getId();
    UUID getExternalId();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    Long getUserId();
}
