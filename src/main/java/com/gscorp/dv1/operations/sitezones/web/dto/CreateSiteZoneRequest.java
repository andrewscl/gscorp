package com.gscorp.dv1.operations.sitezones.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateSiteZoneRequest (
    @NotNull(message = "El siteExternalId es obligatorio")
    UUID siteExternalId,

    @NotNull(message = "El nombre de la zona es obligatorio")
    String name

){
    
}
