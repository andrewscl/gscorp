package com.gscorp.dv1.shifts.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record CreateShiftRequest(
    Long siteId,
    String code,
    String type, // Puede ser un enum en tu modelo: "FIXED" o "SPORADIC"
    LocalDate startDate,
    LocalDate endDate,
    String weekDays,        // Ejemplo: "MONDAY,WEDNESDAY" (puede ir como lista también)
    OffsetDateTime shiftDateTime, // Para tipo SPORADIC
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    String status,          // Puede ser un enum en tu modelo: "PENDING", "CONFIRMED", etc.
    String description
) {}
