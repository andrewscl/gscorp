package com.gscorp.dv1.sites.web.dto;

import com.gscorp.dv1.sites.infrastructure.SiteProjection;

public record SiteDtoProjection (
    Long id,
    String name,
    String address,
    Double lat,
    Double lon,
    String timeZone
){
    public static SiteDtoProjection fromEntity(SiteProjection site){

        if(site == null) return null;

        return new SiteDtoProjection(
            site.getId(),
            site.getName(),
            site.getAddress(),
            site.getLat(),
            site.getLon(),
            site.getTimeZone()
        );
    }
}
