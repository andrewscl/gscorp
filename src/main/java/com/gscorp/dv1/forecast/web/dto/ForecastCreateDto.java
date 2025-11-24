package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gscorp.dv1.enums.ForecastMetric;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO inmutable para la creación de Forecasts (form binding friendly).
 *
 * - periodStart/periodEnd: LocalDate (inputs type="date").
 * - periodStartHour/periodEndHour: optional Integer 0..23 (useful for HOURLY).
 * - La conversión a OffsetDateTime se hace en el service/mapper usando la tz (ZoneId).
 *
 * Nota: este DTO no contiene lógica de negocio ni conversiones a OffsetDateTime.
 */
public record ForecastCreateDto(
    Long clientId,
    Long projectId,
    Long siteId,

    @NotNull(message = "metric is required")
    ForecastMetric forecastMetric,

    @NotNull(message = "periodicity is required")
    String periodicity,

    @NotNull(message = "periodStart is required")
    LocalDate periodStart,

    LocalDate periodEnd,

    /**
     * Optional hour for hourly forecast. Null for date-only periodicities.
     * Should be 0..23 if present.
     */
    Integer periodStartHour,

    Integer periodEndHour,

    @NotNull(message = "value is required")
    @DecimalMin(value = "0", message = "value must be >= 0")
    BigDecimal value,

    String units,

    @DecimalMin(value = "0", message = "confidence must be >= 0")
    @DecimalMax(value = "100", message = "confidence must be <= 100")
    BigDecimal confidence,

    String note,
    Integer forecastVersion,
    String tz
) {}