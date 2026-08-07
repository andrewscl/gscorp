package com.gscorp.dv1.operations.shiftpatterns.web.dto;

import java.util.UUID;

import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;

public record ShiftPatternDto (
    Long id,
    UUID externalId,
    String name,
    String description,
    Long workDays,
    Long restDays,
    String code
){
    public static ShiftPatternDto fromEntity(ShiftPattern sp) {
        if (sp == null) return null;
        return new ShiftPatternDto(
            sp.getId(),
            sp.getExternalId(),
            sp.getName(),
            sp.getDescription(),
            sp.getWorkDays(),
            sp.getRestDays(),
            sp.getCode()
        );
    }
}
