package com.gscorp.dv1.operations.shiftpatterns.web.dto;

public record UpdateShiftPatternRequest (
    String name,
    String code,
    String description,
    Long workDays,
    Long restDays
){}
