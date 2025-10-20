package com.gscorp.dv1.requests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateShiftRequestRequest (

    @NotNull @Size(min=2, max=16) String code,
    @NotNull Long siteId,
    @NotNull String type, // "FIXED" or "SPORADIC"
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String weekDays, // e.g. "MONDAY,WEDNESDAY"
    LocalDateTime shiftDateTime, // for SPORADIC
    @NotNull LocalTime startTime, // shift start time
    @NotNull LocalTime endTime,   // shift end time
    LocalTime lunchTime,          // shift lunch time
    @NotNull String status,       // "PENDING", "CONFIRMED", "CANCELLED"
    @Size(max=1024) String description

){}
