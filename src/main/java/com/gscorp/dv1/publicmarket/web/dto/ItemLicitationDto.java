package com.gscorp.dv1.publicmarket.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemLicitationDto(
    @JsonProperty("CodigoProducto")
    String code,

    @JsonProperty("NombreProducto")
    String name,

    @JsonProperty("Descripcion")
    String description,

    @JsonProperty("Cantidad")
    Double quantity,

    @JsonProperty("Unidad")
    String unit,

    @JsonProperty("PrecioUnitario")
    Double estimatedAmount,   // Si el JSON es unitario, puedes sumarlo * cantidad después

    @JsonProperty("Moneda")
    String currency,

    @JsonProperty("Categoria")
    String category,

    @JsonProperty("SubCategoria")
    String subCategory

) {}