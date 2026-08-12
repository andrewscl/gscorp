package com.gscorp.dv1.operations.shifts.infrastructure.projections;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftStatus;

public interface ShiftProjection {

    Long getId();
    UUID getExternalId();
    LocalDate getShiftDate();
    OffsetDateTime getStartTs();
    OffsetDateTime getEndTs();
    ShiftStatus getStatus();
    String getSiteName();
    String getShiftRequestCode();
    String getEmployeeName();
    String getEmployeeFatherSurname();

}
