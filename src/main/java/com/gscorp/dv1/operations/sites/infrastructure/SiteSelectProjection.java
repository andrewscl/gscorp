package com.gscorp.dv1.operations.sites.infrastructure;

import java.util.UUID;

public interface SiteSelectProjection {
    Long getId();
    UUID getExternalId();
    String getName();
    Double getLat();
    Double getLon();
}
