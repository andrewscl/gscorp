package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;

public record ShiftRequestDtoWithSchedules(
    Long id,
    String code,
    SiteDto site,
    Long clientAccountId,
    ShiftRequestType type,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    String description,
    LocalDateTime createdAt,
    List<ShiftRequestScheduleDto> schedules
) {
    public static ShiftRequestDtoWithSchedules fromEntity(ShiftRequest sr) {
        if (sr == null) return null;
        return new ShiftRequestDtoWithSchedules(
            sr.getId(),
            sr.getCode(),
            sr.getSite() == null ? null : new SiteDto(sr.getSite().getId(), sr.getSite().getName()),
            sr.getClientAccountId(),
            sr.getType(),
            sr.getStartDate(),
            sr.getEndDate(),
            sr.getStatus() != null ? sr.getStatus().name() : null,
            sr.getDescription(),
            sr.getCreatedAt(),
            sr.getSchedules() == null
                ? List.of()
                : sr.getSchedules()
                    .stream()
                    .map(ShiftRequestScheduleDto::fromEntity)
                    .toList()
        );
    }

    public record SiteDto(Long id, String name) {}
}
