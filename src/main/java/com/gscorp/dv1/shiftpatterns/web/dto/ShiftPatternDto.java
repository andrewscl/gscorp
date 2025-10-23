package com.gscorp.dv1.shiftpatterns.web.dto;

import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;

public record ShiftPatternDto (
    Long id,
    String name,
    String description,
    Long workDays,
    Long restDays,
    String code,
    Integer startDay
){
    public static ShiftPatternDto fromEntity(ShiftPattern sp) {
        if (sp == null) return null;
        return new ShiftPatternDto(
            sp.getId(),
            sp.getName(),
            sp.getDescription(),
            sp.getWorkDays(),
            sp.getRestDays(),
            sp.getCode(),
            sp.getStartDay()
        );
    }
}
