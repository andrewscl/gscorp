package com.gscorp.dv1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LicitationDTO {
    
    @JsonProperty("CodigoExterno")
    private String externalCode;

    @JsonProperty("Nombre")
    private String name;

    @JsonProperty("FechaPublicacion")
    private String publishDate;

    @JsonProperty("FechaCierre")
    private String closeDate;

    @JsonProperty("Comprador")
    private BuyerDTO buyer;
}
