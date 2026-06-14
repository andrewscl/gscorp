package com.gscorp.dv1.attendance.web.dto;

public record DashboardHeaderInfo (
    String greeting,
    String emoji,
    String message,
    String lastPunchText,
    String nextAction
){}
