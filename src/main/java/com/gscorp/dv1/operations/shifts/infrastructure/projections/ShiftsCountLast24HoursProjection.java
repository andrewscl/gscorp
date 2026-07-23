package com.gscorp.dv1.operations.shifts.infrastructure.projections;

import java.time.OffsetDateTime;

public interface ShiftsCountLast24HoursProjection {
    Long getTotalShifts();
    OffsetDateTime getStartTs();
}
