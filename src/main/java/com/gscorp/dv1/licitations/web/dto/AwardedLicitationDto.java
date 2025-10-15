package com.gscorp.dv1.licitations.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AwardedLicitationDto(

    @JsonProperty("ProveedorNombre")
    String supplierName,

    @JsonProperty("ProveedorRut")
    String supplierRut,

    @JsonProperty("MontoAdjudicado")
    Double amount,

    @JsonProperty("Moneda")
    String currency,

    @JsonProperty("FechaAdjudicacion")
    String awardDate

    // ...otros campos relevantes...
) {}