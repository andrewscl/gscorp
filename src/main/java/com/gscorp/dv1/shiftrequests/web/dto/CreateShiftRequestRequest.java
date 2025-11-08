package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateShiftRequestRequest(
    Long siteId,
    String type,    // "FIXED" o "SPORADIC"
    Long accountId,                 
    LocalDate startDate,
    LocalDate endDate,
    String description,
    List<ShiftScheduleRequest> schedules
) {}