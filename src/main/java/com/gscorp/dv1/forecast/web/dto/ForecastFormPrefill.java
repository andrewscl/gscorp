package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;

public record ForecastFormPrefill (
    Long clientId,
    Long projectId,
    Long siteId,
    ForecastMetric forecastMetric,
    Periodicity periodicity,
    LocalDate periodStart,
    LocalDate periodEnd,
    Integer periodStartHour,
    Integer periodEndHour,
    BigDecimal value,
    String units,
    BigDecimal confidence,
    String note,
    Integer forecastVersion,
    String tz
){
    
}
