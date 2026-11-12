package com.gscorp.dv1.operations.forecast.web.dto;

import java.util.List;

import com.gscorp.dv1.admin.clients.web.dto.ClientDto;

public record ForecastFormPayload (
    ForecastFormPrefill prefill,
    List<ClientDto> clients
){}
