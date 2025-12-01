package com.gscorp.dv1.sitevisits.infrastructure;

import java.time.OffsetDateTime;

public interface SiteVisitDtoProjection {

    Long getId();
    Long getEmployeeId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    Long getSiteId();
    String getSiteName();
    OffsetDateTime getVisitDateTime();
    Double getLatitude();
    Double getLongitude();
    String getDescription();
    String getPhotoPath();
    String getVideoPath();
    String getClientTimezone();   // importante para formateo en service (IANA preferible)
    String getTimezoneSource();

}
