package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

public interface SiteVisitHourlyCountProjection {

    Long getSiteId();
    String getSiteName();
    String getHour();
    Number getCount();

}
