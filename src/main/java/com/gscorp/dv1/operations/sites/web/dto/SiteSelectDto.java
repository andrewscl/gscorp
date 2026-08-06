package com.gscorp.dv1.operations.sites.web.dto;

import java.util.UUID;

import com.gscorp.dv1.operations.sites.infrastructure.SiteProjection;

public record SiteSelectDto (
    Long id,
    UUID externalId,
    String name,
    Double lat,
    Double lon
){
    public static SiteSelectDto fromProjection(SiteProjection p){
        if(p == null) return null;
        return new SiteSelectDto(
            p.getId(),
            p.getExternalId(),
            p.getName(),
            p.getLat(),
            p.getLon()
        );
    }
}
