package com.gscorp.dv1.hr.web.dto;

public record ClientStatDto (
    String clientName,
    long activeCount,
    long pendingCount
){}

