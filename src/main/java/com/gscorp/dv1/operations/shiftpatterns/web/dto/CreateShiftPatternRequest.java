package com.gscorp.dv1.operations.shiftpatterns.web.dto;

public record CreateShiftPatternRequest(
    String name,
    String code,
    String description,
    Long workDays,
    Long restDays
) {}
