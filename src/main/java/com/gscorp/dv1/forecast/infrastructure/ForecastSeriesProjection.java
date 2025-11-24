package com.gscorp.dv1.forecast.infrastructure;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface ForecastSeriesProjection {
    OffsetDateTime getPeriodStart();
    BigDecimal getValue();
}
