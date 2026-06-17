package com.gscorp.dv1.hr.web.dto;

public record ClientStatDto (
    String clientName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){}

