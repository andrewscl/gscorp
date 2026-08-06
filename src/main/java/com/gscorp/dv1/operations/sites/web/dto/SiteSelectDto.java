package com.gscorp.dv1.operations.sites.web.dto;

import com.gscorp.dv1.operations.sites.infrastructure.SiteProjection;

public record SiteSelectDto (
    Long id,
    String name,
    Double lat,
    Double lon
){
    public static SiteSelectDto fromProjection(SiteProjection p){
        if(p == null) return null;
        return new SiteSelectDto(
            p.getId(),
            p.getName(),
            p.getLat(),
            p.getLon()
        );
    }
}
