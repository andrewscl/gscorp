package com.gscorp.dv1.forecast.infrastructure;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;

public interface ForecastSeriesProjection {
    OffsetDateTime getPeriodStart();
    OffsetDateTime getPeriodEnd();
    BigDecimal getValue();
    Periodicity getPeriodicity();
    ForecastMetric getMetric();
    Long getSiteId();
    Long getProjectId();
}
