package com.gscorp.dv1.sites.web.dto;

import java.util.UUID;

import com.gscorp.dv1.sites.infrastructure.Site;

public record SiteDto (
    Long id,
    UUID externalId,
    Long projectId,
    String projectName,
    String name,
    String address,
    String timeZone,
    Double lat,
    Double lon,
    Boolean active
) {
    public static SiteDto fromEntity ( Site site){

        if(site == null) return null;

        return new SiteDto(
            site.getId(),
            site.getExternalId(),
            site.getProject() != null ? site.getProject().getId() : null,
            site.getProject() != null ? site.getProject().getName() : null,
            site.getName(),
            site.getAddress(),
            site.getTimeZone(),
            site.getLat(),
            site.getLon(),
            site.getActive()
        );
    }
}