package com.gscorp.dv1.operations.sitevisits.infrastructure;

public interface SiteVisitHourlyCountProjection {

    Long getSiteId();
    String getSiteName();
    String getHour();
    Number getCount();

}
