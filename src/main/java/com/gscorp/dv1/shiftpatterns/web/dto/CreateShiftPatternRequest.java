package com.gscorp.dv1.shiftpatterns.web.dto;

public record CreateShiftPatternRequest(
    String name,
    String description,
    Long workDays,
    Long restDays,
    String code,
    Integer startDay
) {}

