package com.gscorp.dv1.operations.sitezones.web.dto;

import java.util.UUID;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZone;
import com.gscorp.dv1.operations.sitezones.infrastructure.projections.SiteZoneProjection;

public record SiteZoneDto (
    Long id,
    UUID externalId,
    String name,
    SiteZoneStatus status
){
    public static SiteZoneDto fromProjection(SiteZoneProjection projection) {
        if (projection == null) return null;
        return new SiteZoneDto(
            projection.getId(),
            projection.getExternalId(),
            projection.getName(),
            projection.getStatus()
        );
    }
    public static SiteZoneDto fromEntity(SiteZone entity) {
        if (entity == null) return null;
        return new SiteZoneDto(
            entity.getId(),
            entity.getExternalId(),
            entity.getName(),
            entity.getStatus()
        );
    }
}
