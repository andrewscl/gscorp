package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.util.List;

import com.gscorp.dv1.enums.RequestType;

public record CreateShiftRequestRequest(
    Long siteId,
    RequestType type,    // "FIXED" o "SPORADIC"
    Long accountId,                 
    LocalDate startDate,
    LocalDate endDate,
    String description,
    List<ShiftScheduleRequest> schedules
) {}