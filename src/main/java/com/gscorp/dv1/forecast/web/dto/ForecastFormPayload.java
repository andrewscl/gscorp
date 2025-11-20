package com.gscorp.dv1.forecast.web.dto;

import java.util.List;

import com.gscorp.dv1.clients.web.dto.ClientBriefDto;

public record ForecastFormPayload (
    ForecastFormPrefill prefill,
    List<ClientBriefDto> clients
){}
