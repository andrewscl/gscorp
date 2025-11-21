package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.Units;

public record ForecastTableRowDto (
    Long id,
    Long projectId,
    String projectName,
    Long siteId,
    String siteName,
    String metric,
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
            String metric,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            BigDecimal value,
            Units units,
            Boolean isActive) {
        return new ForecastTableRowDto(id, projectId, projectName, siteId, siteName,
                metric, periodStart, periodEnd, value, units, isActive);
    }

}
