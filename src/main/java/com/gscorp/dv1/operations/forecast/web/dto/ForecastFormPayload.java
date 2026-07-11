package com.gscorp.dv1.operations.forecast.web.dto;

import java.util.List;

import com.gscorp.dv1.admin.clients.web.dto.ClientSelectDto;

public record ForecastFormPayload (
    ForecastFormPrefill prefill,
    List<ClientSelectDto> clients
){}
