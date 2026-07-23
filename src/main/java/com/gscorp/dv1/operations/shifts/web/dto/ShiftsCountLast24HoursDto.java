package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftsCountLast24HoursProjection;

public record ShiftsCountLast24HoursDto (
    Long totalShifts,
    OffsetDateTime startTs
){
    public static ShiftsCountLast24HoursDto
            fromProjection(ShiftsCountLast24HoursProjection p){
        if (p == null) return null;
        return new ShiftsCountLast24HoursDto(
            p.getTotalShifts(),
            p.getStartTs()
        );
    }

}
