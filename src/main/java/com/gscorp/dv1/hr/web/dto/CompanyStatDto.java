package com.gscorp.dv1.hr.web.dto;

public record CompanyStatDto (
    String companyName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount
){}
