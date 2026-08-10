package com.gscorp.dv1.operations.shiftassignments.infrastructure.projections;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;

public interface ShiftAssignmentProjection {
    Long getId();
    UUID getExternalId();
    String getSiteName();
    UUID getShiftRequestExternalId();
    Long getShiftRequestId();
    String getShiftRequestCode();
    UUID getEmployeeExternalId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    String getEmployeeRut();
    ShiftAssignmentStatus getStatus();
    String getNotes();
    OffsetDateTime getAssignedAt();
    OffsetDateTime getAssignedUntil();
    Integer getStartCycleNumber();
    String getCreatedBy();
    String getUpdatedBy();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
}
