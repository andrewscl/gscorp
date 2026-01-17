package com.gscorp.dv1.sites.infrastructure;

public interface SiteProjection {

    Long getId();
    String getName();
    String getAddress();
    Double getLat();
    Double getLon();
    String getTimeZone();

}
