package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;

public record ForecastPointDto (
    String date,
    BigDecimal value
){
    
}
