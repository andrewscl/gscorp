package com.gscorp.dv1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BuyerDTO {
    
    @JsonProperty("NombreOrganismo")
    private String name;

    @JsonProperty("CodigoOrganismo")
    private String code;
}