package com.gscorp.dv1.operations.sites.infrastructure;

import java.util.UUID;

public interface SiteProjection {

    Long getId();
    UUID getExternalId();
    String getName();
    String getAddress();
    Double getLat();
    Double getLon();
    String getTimeZone();

}
