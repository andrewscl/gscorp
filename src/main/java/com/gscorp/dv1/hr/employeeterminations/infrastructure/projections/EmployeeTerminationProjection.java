package com.gscorp.dv1.hr.employeeterminations.infrastructure.projections;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.enums.TerminationReason;

public interface EmployeeTerminationProjection {
    Long getId();
    UUID getExternalId();
    TerminationReason getTerminationReason();
    EmployeeTransitionStatus getStatus();
    LocalDate getProposedExitDate();
    String getDescription();
    String getResolvedBy();
    OffsetDateTime getResolvedAt();
    String getCreatedBy();
    String getUpdatedBy();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    Long getEmployeeId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    String getRut();
}
