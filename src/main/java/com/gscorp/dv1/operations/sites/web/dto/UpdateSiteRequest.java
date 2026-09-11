package com.gscorp.dv1.operations.sites.web.dto;

import com.gscorp.dv1.enums.SiteStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSiteRequest (
    @NotNull @Size(min=2, max=160) String name,
    String address,
    Double lat,
    Double lon,
    String timeZone,
    SiteStatus status,
    Boolean active
){}

