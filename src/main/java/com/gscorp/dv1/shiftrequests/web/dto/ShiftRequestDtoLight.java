package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestProjection;

public record ShiftRequestDtoLight(
        Long id,
        String code,
        Long siteId,
        String siteName,
        Long clientAccountId,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String description,
        LocalDateTime createdAt,
        Integer schedulesCount
    ) {
        public static ShiftRequestDtoLight fromProjection(ShiftRequestProjection sr) {
            if (sr == null) return null;
            return new ShiftRequestDtoLight(
                sr.getId(),
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
