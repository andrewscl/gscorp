package com.gscorp.dv1.operations.sites.web.dto;

import java.util.UUID;

import com.gscorp.dv1.operations.sites.infrastructure.SiteProjection;

public record SiteDtoProjection (
    Long id,
    UUID externalId,
    String name,
    String address,
    Double lat,
    Double lon,
    String timeZone
){
    public static SiteDtoProjection fromProjection(SiteProjection site){

        if(site == null) return null;

        return new SiteDtoProjection(
            site.getId(),
            site.getExternalId(),
            site.getName(),
            site.getAddress(),
            site.getLat(),
            site.getLon(),
            site.getTimeZone()
        );
    }
}
