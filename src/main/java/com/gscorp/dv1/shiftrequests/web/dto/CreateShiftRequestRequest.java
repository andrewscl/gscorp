package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CreateShiftRequestRequest(
    String code,
    Long siteId,
    String type,                 // "FIXED" o "SPORADIC"
    LocalDate startDate,
    LocalDate endDate,
    String weekDays,             // Para FIXED
    LocalDateTime shiftDateTime, // Para SPORADIC
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    String status,               // PENDING, CONFIRMED, CANCELLED
    String description
) {}