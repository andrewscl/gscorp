package com.gscorp.dv1.attendance.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunchProjection;

public record AttendancePunchDto (
    Long id,
    Long userId,
    Long employeeId,
    String employeeName,
    String employeeFatherSurname,
    Long siteId,
    String siteName,
    OffsetDateTime ts,
    Double lat,
    Double lon,
    Double accuracyM,
    String action,
    Boolean locationOk,
    Double distanceM,
    String deviceInfo,
    String ip,
    String timezoneSource,
    String createdAt,
    String updatedAt,
    String tsFormatted
) {
    public static AttendancePunchDto fromProjection(AttendancePunchProjection ap, String tsFormatted) {
        if (ap == null) return null;

        return new AttendancePunchDto(
            ap.getId(),
            ap.getUserId(),
            ap.getEmployeeId(),
            ap.getEmployeeName(),
            ap.getEmployeeFatherSurname(),
            ap.getSiteId(),
            ap.getSiteName(),
            ap.getTs(),
            ap.getLat(),
            ap.getLon(),
            ap.getAccuracyM(),
            ap.getAction(),
            ap.getLocationOk(),
            ap.getDistanceM(),
            ap.getDeviceInfo(),
            ap.getIp(),
            ap.getTimezoneSource(),
            ap.getCreatedAt(),
            ap.getUpdatedAt(),
            tsFormatted
        );
    }

    // Versión de compatibilidad sin formatted string
    public static AttendancePunchDto fromProjection(AttendancePunchProjection ap) {
        return fromProjection(ap, null);
    }
}
