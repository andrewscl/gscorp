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
    TerminationReason getFinalTerminationReason();
    EmployeeTransitionStatus getStatus();
    LocalDate getProposedExitDate();
    LocalDate getFinalExitDate();
    String getDescription();
    String getResolvedBy();
    OffsetDateTime getResolvedAt();
    String getCreatedBy();
    String getUpdatedBy();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    Long getEmployeeId();
    UUID getEmployeeExternalId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    String getRut();
}
