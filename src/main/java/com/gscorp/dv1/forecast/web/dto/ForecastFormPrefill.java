package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gscorp.dv1.enums.ForecastCategory;

public record ForecastFormPrefill (
    Long clientId,
    Long projectId,
    Long siteId,
    String periodicity,
    String metric,
    ForecastCategory forecastCategory,
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
