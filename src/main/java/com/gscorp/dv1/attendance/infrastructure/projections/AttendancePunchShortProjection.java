package com.gscorp.dv1.attendance.infrastructure.projections;

import java.time.OffsetDateTime;

public interface AttendancePunchShortProjection {
    Long getId();
    Long getUserId();
    String getAction();
    OffsetDateTime getTs();
    String getClientTimezone();
}
