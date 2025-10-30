package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;

public record ShiftRequestDto(
    Long id,
    String code,
    SiteDto site,
    String type,
    LocalDate startDate,
    LocalDate endDate,
    String weekDays,
    LocalDateTime shiftDateTime,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    String status,
    String description,
    LocalDateTime createdAt
) {
    public static ShiftRequestDto fromEntity(ShiftRequest sr) {
        if (sr == null) return null;
        return new ShiftRequestDto(
            sr.getId(),
            sr.getCode(),
            sr.getSite() == null ? null : new SiteDto(sr.getSite().getId(), sr.getSite().getName()),
            sr.getType() == null ? null : sr.getType().name(),
            sr.getStartDate(),
            sr.getEndDate(),
            sr.getWeekDays(),
            sr.getShiftDateTime(),
            sr.getStartTime(),
            sr.getEndTime(),
            sr.getLunchTime(),
            sr.getStatus(),
            sr.getDescription(),
            sr.getCreatedAt()
        );
    }

    public record SiteDto(Long id, String name) {}
}