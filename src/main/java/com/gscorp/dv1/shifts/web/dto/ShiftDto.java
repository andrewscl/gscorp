package com.gscorp.dv1.shifts.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.enums.ShiftType;

public record ShiftDto(
    Long id,
    Long siteId,
    String siteName,
    OffsetDateTime startTs,
    OffsetDateTime endTs,
    String code,
    String description,
    ShiftType shiftType,
    String weekDays,
    Integer lunchTime,
    ShiftStatus shiftStatus,
    Integer plannedGuards,
    ShiftRequestIdDto shiftRequestId
) {}