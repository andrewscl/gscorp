package com.gscorp.dv1.operations.shifts.infrastructure.projections;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ShiftProjection {

    Long getId();
    UUID getExternalId();
    LocalDate getShiftDate();
    OffsetDateTime getStartTs();
    OffsetDateTime getEndTs();
}
