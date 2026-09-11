package com.gscorp.dv1.operations.sites.infrastructure;

import java.util.UUID;

import com.gscorp.dv1.enums.SiteStatus;

public interface SiteProjection {

    Long getId();
    UUID getExternalId();
    String getName();
    String getAddress();
    Double getLat();
    Double getLon();
    String getTimeZone();
    Long getProjectId();
    String getProjectName();
    SiteStatus getStatus();
    Boolean getActive();
}
