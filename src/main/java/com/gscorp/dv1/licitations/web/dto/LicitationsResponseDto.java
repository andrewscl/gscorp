package com.gscorp.dv1.licitations.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LicitationsResponseDto (

    @JsonProperty("Cantidad")
    Integer cantidad,

    @JsonProperty("FechaCreacion")
    String fechaCreacion,

    @JsonProperty("Version")
    String version,

    @JsonProperty("Listado")
    List<LicitationDto> listado
    
){}