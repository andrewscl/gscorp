package com.gscorp.dv1.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LicitationsResponse {

    @JsonProperty("Cantidad")
    private Integer cantidad;

    @JsonProperty("FechaCreacion")
    private String fechaCreacion;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("Listado")
    private List<LicitationDTO> listado;
    
}