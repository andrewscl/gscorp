package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ForecastCategory;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.forecast.infrastructure.Forecast;

public record ForecastRecordDto (
    Long id,
    Long clientId,
    Long projectId,
    Long siteId,
    ForecastCategory forecastCategory,
    Periodicity periodicity,
    String metric,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd,
    Integer periodStartHour,
    Integer periodEndHour,
    BigDecimal value,
    Units units,
    String tz,
    String note,
    BigDecimal confidence,
    Integer forecastVersion,
    Long createdBy,
    Long updatedBy,
    Instant createdAt,
    Instant updatedAt,
    Boolean isActive,
    Long rowVersion
){
        /**
     * Create a ForecastRecord from the JPA Forecast entity.
     * Uses the fully-qualified entity type to avoid name collisions.
     *
     * @param entity the JPA Forecast entity (may be null)
     * @return a ForecastRecord or null if entity was null
     */
    public static ForecastRecordDto fromEntity(Forecast entity) {
        if (entity == null) return null;

        return new ForecastRecordDto(
            entity.getId(),
            entity.getClientId(),
            entity.getProjectId(),
            entity.getSiteId(),
            entity.getForecastCategory(),
            entity.getPeriodicity(),
            entity.getMetric(),
            entity.getPeriodStart(),
            entity.getPeriodEnd(),
            entity.getPeriodStartHour(),
            entity.getPeriodEndHour(),
            entity.getValue(),
            entity.getUnits(),
            entity.getTz(),
            entity.getNote(),
            entity.getConfidence(),
            entity.getForecastVersion(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getIsActive(),
            entity.getRowVersion()
        );
    }
}
