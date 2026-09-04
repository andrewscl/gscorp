package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestProjection;

public record ShiftRequestSelectDto (
    Long id,
    UUID externalId,
    String code,
    Long siteId,
    String siteName,
    String shiftPatternName,
    Long clientAccountId,
    ShiftRequestType type,
    LocalDate startDate,
    LocalDate endDate,
    ShiftRequestStatus status,
    LocalDateTime createdAt,
    String description,
    List<ShiftRequestScheduleStrDto> schedules
){
    public static ShiftRequestSelectDto fromProjection(
        ShiftRequestProjection pr, 
        List<ShiftRequestScheduleStrDto> schedules
    ) {
        if (pr == null) return null;
        return new ShiftRequestSelectDto(
            pr.getId(),
            pr.getExternalId(),
            pr.getCode(),
            pr.getSiteId(),
            pr.getSiteName(),
            pr.getShiftPatternName(),
            pr.getClientAccountId(),
            pr.getType(),
            pr.getStartDate(),
            pr.getEndDate(),
            pr.getStatus(),
            pr.getCreatedAt(),
            pr.getDescription(),
            schedules == null ? List.of() : schedules
        );
    }

    public String getFormattedSchedules() {
        if (schedules == null || schedules.isEmpty()) {
            return "-";
        }
        return schedules.stream()
            .map(s -> s != null ? s.toDisplayString() : "")
            .filter(str -> !str.isBlank())
            .collect(Collectors.joining("<br/>"));
    }

}
