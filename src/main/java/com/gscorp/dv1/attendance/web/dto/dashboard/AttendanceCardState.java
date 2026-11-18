package com.gscorp.dv1.attendance.web.dto.dashboard;

import com.gscorp.dv1.enums.AttendanceCardStatus;

public record AttendanceCardState (
    AttendanceCardStatus status,
    String nextAction,
    String statusText,
    String lastPunchText
){}

