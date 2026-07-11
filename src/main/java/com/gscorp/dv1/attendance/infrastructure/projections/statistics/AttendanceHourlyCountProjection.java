package com.gscorp.dv1.attendance.infrastructure.projections.statistics;

public interface AttendanceHourlyCountProjection {
    String getHour();
    Long getCnt();
}
