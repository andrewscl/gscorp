package com.gscorp.dv1.operations.shifts.infrastructure.projections;

import java.time.OffsetDateTime;

public interface ShiftsCountLast24HoursProjection {
    Long getTotalShifts();
    Long getUnplannedShifts();
    Long getPlannedShifts();
    Long getInProgressShifts();
    Long getCompletedShifts();
    Long getCancelledShifts();
    Long getPendingShifts();
    Long getUncoveredShifts();
    OffsetDateTime getStartTs();
}
