package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestProjection;

public record ShiftRequestSelectDto (
    Long id,
    UUID externalId,
    String code,
    LocalDate startDate,
    LocalDate endDate,
    List<ShiftRequestScheduleDto> schedules
){
    public static ShiftRequestSelectDto fromProjection(
        ShiftRequestProjection pr, 
        List<ShiftRequestScheduleDto> schedules
    ) {
        if (pr == null) return null;
        return new ShiftRequestSelectDto(
            pr.getId(),
            pr.getExternalId(),
            pr.getCode(),
            pr.getStartDate(),
            pr.getEndDate(),
            schedules == null ? List.of() : schedules
        );
    }
}
