package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestProjection;

public record ShiftRequestDto(
        Long id,
        UUID externalId,
        String code,
        Long siteId,
        String siteName,
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

}
