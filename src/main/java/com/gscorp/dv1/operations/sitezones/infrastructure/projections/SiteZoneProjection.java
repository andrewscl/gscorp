package com.gscorp.dv1.operations.sitezones.infrastructure.projections;

import java.util.UUID;

import com.gscorp.dv1.enums.SiteZoneStatus;

public interface SiteZoneProjection {
    Long getId();
    UUID getExternalId();
    String getName();
    SiteZoneStatus getStatus();
}
