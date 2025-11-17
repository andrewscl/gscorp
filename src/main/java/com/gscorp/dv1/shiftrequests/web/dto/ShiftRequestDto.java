package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;

public record ShiftRequestDto(
    Long id,
    String code,
    SiteDto site,
    Long clientAccountId,
    String type,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    String description,
    LocalDateTime createdAt,
    List<ShiftScheduleDto> schedules
) {
    public static ShiftRequestDto fromEntity(ShiftRequest sr) {
        if (sr == null) return null;
        return new ShiftRequestDto(
            sr.getId(),
            sr.getCode(),
            sr.getSite() == null ? null : new SiteDto(sr.getSite().getId(), sr.getSite().getName()),
            sr.getClientAccountId(),
            sr.getType() == null ? null : sr.getType().name(),
            sr.getStartDate(),
            sr.getEndDate(),
            sr.getStatus() != null ? sr.getStatus().name() : null,
            sr.getDescription(),
            sr.getCreatedAt(),
            sr.getSchedules() == null
                ? List.of()
                : sr.getSchedules()
                    .stream()
                    .map(ShiftScheduleDto::fromEntity)
                    .toList()
        );
    }

    public record SiteDto(Long id, String name) {}
}
