package com.gscorp.dv1.operations.forecast.web.dto;

import java.math.BigDecimal;

public record ForecastPointDto (
    String date,
    BigDecimal value
){
    
}
