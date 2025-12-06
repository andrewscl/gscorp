package com.gscorp.dv1.forecast.web.dto;

import java.util.List;

import com.gscorp.dv1.clients.web.dto.ClientSelectDto;

public record ForecastFormPayload (
    ForecastFormPrefill prefill,
    List<ClientSelectDto> clients
){}
