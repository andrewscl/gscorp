package com.gscorp.dv1.attendance.infrastructure;

import java.time.OffsetDateTime;

public interface AttendancePunchProjection {

    Long getId();
    Long getUserId();
    Long getSiteId();
    String getSiteName();
    OffsetDateTime getTs();
    Double getLat();
    Double getLon();
    Double getAccuracyM();
    String getAction();
    Boolean getLocationOk();
    Double getDistanceM();
    String getDeviceInfo();
    String getIp();
    Long getEmployeeId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    String getClientTimezone();
    String getTimezoneSource();
    String getCreatedAt();
    String getUpdatedAt();

}
