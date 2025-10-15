package com.gscorp.dv1.licitations.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LicitationDto(

    @JsonProperty("CodigoExterno")
    String externalCode,

    @JsonProperty("Nombre")
    String name,

    @JsonProperty("Descripcion")
    String description,

    @JsonProperty("Estado")
    String status,

    @JsonProperty("Tipo")
    String type,

    @JsonProperty("CompradorNombre")
    String buyerName,

    @JsonProperty("CompradorRut")
    String buyerRut,

    @JsonProperty("FechaPublicacion")
    String publishDate,

    @JsonProperty("FechaCierre")
    String closeDate,

    @JsonProperty("FechaApertura")
    String openDate,

    @JsonProperty("FechaAdjudicacion")
    String awardDate,

    @JsonProperty("MontoEstimado")
    Double estimatedAmount,

    @JsonProperty("Moneda")
    String currency,

    @JsonProperty("Categoria")
    String category,

    @JsonProperty("Sector")
    String sector,

    @JsonProperty("SubCategoria")
    String subCategory,

    @JsonProperty("Region")
    String region,

    @JsonProperty("Commune")
    String commune,

    @JsonProperty("ContactoNombre")
    String contactName,

    @JsonProperty("ContactoEmail")
    String contactEmail,

    @JsonProperty("ContactoTelefono")
    String contactPhone,

    @JsonProperty("BasesUrl")
    String basesUrl,

    @JsonProperty("RecordUrl")
    String recordUrl,

    @JsonProperty("JsonData")
    String jsonData,

    @JsonProperty("Items")
    List<ItemLicitationDto> items,

    @JsonProperty("Awarded")
    List<AwardedLicitationDto> awarded

) {}