package com.gscorp.dv1.rrhh.web.dto;

public record ClientStatDto (
    String clientName,
    Long activeCount,
    Long pendingCount
){}

