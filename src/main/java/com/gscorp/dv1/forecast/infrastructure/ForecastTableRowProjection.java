package com.gscorp.dv1.forecast.infrastructure;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.Units;

public interface ForecastTableRowProjection {
    Long getId();
    Long getProjectId();
    String getProjectName();
    Long getSiteId();
    String getSiteName();
    String getMetric();
    OffsetDateTime getPeriodStart();
    OffsetDateTime getPeriodEnd();
    BigDecimal getValue();
    Units getUnits();
    Boolean getIsActive();
    
}
