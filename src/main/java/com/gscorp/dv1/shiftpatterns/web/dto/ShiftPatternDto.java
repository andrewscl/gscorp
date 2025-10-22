package com.gscorp.dv1.shiftpatterns.web.dto;

public record ShiftPatternDto (
    Long id,
    String name,
    String description,
    Long workDays,
    Long restDays
){
    public static ShiftPatternDto fromEntity(com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern sp) {
        if (sp == null) return null;
        return new ShiftPatternDto(
            sp.getId(),
            sp.getName(),
            sp.getDescription(),
            sp.getWorkDays(),
            sp.getRestDays()
        );
    }
}
