package com.gscorp.dv1.hr.web.dto;

public record CompanyStatDto (
    String companyName,
    long activeCount,
    long pendingCount,
    long noticeOfTerminationCount
){}
