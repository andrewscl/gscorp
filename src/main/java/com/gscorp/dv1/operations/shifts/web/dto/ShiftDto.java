package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftProjection;

public record ShiftDto (
    Long id,
    UUID externalId,
    LocalDate shiftDate,
    OffsetDateTime startTs,
    OffsetDateTime endTs
){
    public static ShiftDto fromProjection(ShiftProjection sp){
        if ( sp == null) return null;
        return new ShiftDto(
            sp.getId(),
            sp.getExternalId(),
            sp.getShiftDate(),
            sp.getStartTs(),
            sp.getEndTs()
        );
    }
}
