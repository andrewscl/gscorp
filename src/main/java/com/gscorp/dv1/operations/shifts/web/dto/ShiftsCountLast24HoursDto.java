package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftsCountLast24HoursProjection;

public record ShiftsCountLast24HoursDto (
    Long totalShifts,
    Long unplannedShifts,
    Long plannedShifts,
    Long inProgressShifts,
    Long completedShifts,
    Long cancelledShifts,
    Long pendingShifts,
    Long uncoveredShifts,
    OffsetDateTime startTs
){
    public static ShiftsCountLast24HoursDto
            fromProjection(ShiftsCountLast24HoursProjection p){
        if (p == null) return null;
        return new ShiftsCountLast24HoursDto(
            p.getTotalShifts(),
            p.getUnplannedShifts(),
            p.getPlannedShifts(),
            p.getInProgressShifts(),
            p.getCompletedShifts(),
            p.getCancelledShifts(),
            p.getPendingShifts(),
            p.getUncoveredShifts(),
            p.getStartTs()
        );
    }

}       
