package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestProjection;

public record ShiftRequestDto(
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
        String description,
        LocalDateTime createdAt,
        Integer schedulesCount
    ) {
        public static ShiftRequestDto
                fromProjection(ShiftRequestProjection sr) {
            if (sr == null) return null;
            return new ShiftRequestDto(
                sr.getId(),
                sr.getExternalId(),
                sr.getCode(),
                sr.getSiteId(),
                sr.getSiteName(),
                sr.getShiftPatternName(),
                sr.getClientAccountId(),
                sr.getType(),
                sr.getStartDate(),
                sr.getEndDate(),
                sr.getStatus(),
                sr.getDescription(),
                sr.getCreatedAt(),
                sr.getSchedulesCount() == null ? 0 : sr.getSchedulesCount()
            );
        }

        public static ShiftRequestDto
                fromEntity(ShiftRequest sr) {
            if (sr == null) return null;
            int schedulesCount =
                (sr.getSchedules() != null) ? sr.getSchedules().size() : 0;
            return new ShiftRequestDto(
                sr.getId(),
                sr.getExternalId(),
                sr.getCode(),
                sr.getSite() != null ? sr.getSite().getId() : null,
                sr.getSite() != null ? sr.getSite().getName() : null,
                sr.getShiftPattern() != null ? sr.getShiftPattern().getName() : null,
                sr.getClientAccountId(),
                sr.getType(),
                sr.getStartDate(),
                sr.getEndDate(),
                sr.getStatus(),
                sr.getDescription(),
                sr.getCreatedAt(),
                schedulesCount
            );
        }

}
