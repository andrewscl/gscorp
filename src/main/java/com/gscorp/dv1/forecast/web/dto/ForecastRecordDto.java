package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.forecast.infrastructure.Forecast;

public record ForecastRecordDto(
    Long id,
    Long clientId,
    Long projectId,
    Long siteId,
    ForecastMetric forecastMetric,
    Periodicity periodicity,
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
    Instant createdAt,
    Boolean isActive
) {

    public static ForecastRecordDto fromEntity(Forecast e) {
        if (e == null) return null;

        Long createdById = null;
        Object cb = null;
        try {
            cb = e.getCreatedBy();
            if (cb != null) {
                if (cb instanceof Number) {
                    createdById = ((Number) cb).longValue();
                } else {
                    // intenta obtener getId() vía reflection (maneja User o proxy)
                    var m = cb.getClass().getMethod("getId");
                    Object idObj = m.invoke(cb);
                    if (idObj instanceof Number) createdById = ((Number) idObj).longValue();
                }
            }
        } catch (NoSuchMethodException nsme) {
            // no hay getId(): dejar createdById null
        } catch (Exception ex) {
            // cualquier otro problema al extraer id -> dejar null (no propagamos)
        }

        return new ForecastRecordDto(
            e.getId(),
            e.getClientId(),
            e.getProjectId(),
            e.getSiteId(),
            e.getForecastMetric(),
            e.getPeriodicity(),
            e.getPeriodStart(),
            e.getPeriodEnd(),
            e.getPeriodStartHour(),
            e.getPeriodEndHour(),
            e.getValue(),
            e.getUnits(),
            e.getTz(),
            e.getNote(),
            e.getConfidence(),
            e.getForecastVersion(),
            createdById,
            e.getCreatedAt(),
            e.getIsActive()
        );
    }
}