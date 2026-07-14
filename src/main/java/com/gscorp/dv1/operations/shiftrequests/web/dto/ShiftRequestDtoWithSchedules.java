package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;

public record ShiftRequestDtoWithSchedules(
    Long id,
    UUID externalId,
    String code,
    SiteDto site,
    Long clientAccountId,
    ShiftRequestType type,
    LocalDate startDate,
    LocalDate endDate,
    ShiftRequestStatus status,
    String description,
    LocalDateTime createdAt,
    List<ShiftRequestScheduleDto> schedules
) {
    public static ShiftRequestDtoWithSchedules fromEntity(ShiftRequest sr) {
        if (sr == null) return null;
        return new ShiftRequestDtoWithSchedules(
            sr.getId(),
            sr.getExternalId(),
            sr.getCode(),
            sr.getSite() == null ? null : new SiteDto(sr.getSite().getId(), sr.getSite().getName()),
            sr.getClientAccountId(),
            sr.getType(),
            sr.getStartDate(),
            sr.getEndDate(),
            sr.getStatus(),
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
