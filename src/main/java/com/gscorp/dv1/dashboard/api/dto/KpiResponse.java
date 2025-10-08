package com.gscorp.dv1.dashboard.api.dto;

public record KpiResponse (
    Double attendanceRate,  // opcional: proxy (ver nota)
    Double patrolCompliance,
    Long incidentsTotal
){}
