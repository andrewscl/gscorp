package com.gscorp.dv1.sites.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest (
    @NotNull Long clientId,
    @NotNull @Size(min=2, max=160) String name,
    String code,
    String address,
    Double lat,
    Double lon,
    String timeZone,
    Boolean active
){}
