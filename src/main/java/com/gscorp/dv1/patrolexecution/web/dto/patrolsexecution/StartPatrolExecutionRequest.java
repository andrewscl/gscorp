package com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution;

import java.math.BigDecimal;

public record StartPatrolExecutionRequest (
    BigDecimal latitude,
    BigDecimal longitude,
    String clientTimezone,
    String timezoneSource
){}
