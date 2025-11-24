package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;

public record ForecastTableRowDto (
    Long id,
    Long projectId,
    String projectName,
    Long siteId,
    String siteName,
    ForecastMetric forecastMetric,
    Periodicity periodicity,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd,
    BigDecimal value,
    Units units,
    Boolean isActive
) {
    public static ForecastTableRowDto of(
            Long id,
            Long projectId,
            String projectName,
            Long siteId,
            String siteName,
            ForecastMetric forecastMetric,
            Periodicity periodicity,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            BigDecimal value,
            Units units,
            Boolean isActive) {
        return new ForecastTableRowDto(id, projectId, projectName, siteId, siteName,
                forecastMetric, periodicity, periodStart, periodEnd, value, units, isActive);
    }

}
