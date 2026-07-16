package com.gscorp.dv1.hr.employeetransitionrequests.infrastructure.projections;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeRequestStatusType;
import com.gscorp.dv1.enums.EmployeeTransitionRequestStatus;
import com.gscorp.dv1.enums.TerminationReason;

public interface EmployeeTransitionRequestProjection {
    Long getId();
    UUID getExternalId();
    EmployeeRequestStatusType getRequestType();
    TerminationReason getTerminationReason();
    EmployeeTransitionRequestStatus getStatus();
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
