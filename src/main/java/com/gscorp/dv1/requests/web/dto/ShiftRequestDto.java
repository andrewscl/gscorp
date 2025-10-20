package com.gscorp.dv1.requests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.gscorp.dv1.requests.infrastructure.ShiftRequest;

public record ShiftRequestDto (
    Long id,
    String code,
    Long siteId,
    String siteName,
    ShiftRequest.RequestType type, // Enum igual que la entidad
    LocalDate startDate,
    LocalDate endDate,
    String weekDays,
    LocalDateTime shiftDateTime,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    String status,
    String description
) {}