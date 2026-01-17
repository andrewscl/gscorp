package com.gscorp.dv1.sites.infrastructure;

public interface SiteProjection {

    Long id();
    String name();
    String address();
    Double lat();
    Double lon();
    String timeZone();

}
