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
    Long coveredShifts,
    Long uncoveredShifts,
    Long validShifts,
    Double coveredPct,
    Double uncoveredPct,
    OffsetDateTime startTs
){
    public static ShiftsCountLast24HoursDto
            fromProjection(ShiftsCountLast24HoursProjection p){
        if (p == null) return null;
        long total = nz(p.getTotalShifts());
        long cancelled = nz(p.getCancelledShifts());
        long valid = Math.max(total - cancelled, 0);
        long uncovered = Math.min(nz(p.getUncoveredShifts()), valid);
        long covered = valid - uncovered;
        double coveredPct = valid == 0 ? 0.0 : round1( covered * 100.0 / valid) ;
        double uncoveredPct = valid == 0 ? 0.0 : round1( 100.0 - coveredPct) ;
        return new ShiftsCountLast24HoursDto(
            p.getTotalShifts(),
            p.getUnplannedShifts(),
            p.getPlannedShifts(),
            p.getInProgressShifts(),
            p.getCompletedShifts(),
            p.getCancelledShifts(),
            p.getPendingShifts(),
            covered,
            uncovered,
            valid,
            coveredPct,
            uncoveredPct,
            p.getStartTs()
        );
    }

    private static long nz(Long value) {
        return value == null ? 0L : value;
    }
    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

}    
