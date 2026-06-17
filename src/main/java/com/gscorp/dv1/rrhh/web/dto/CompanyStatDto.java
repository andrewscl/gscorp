package com.gscorp.dv1.rrhh.web.dto;

public record CompanyStatDto (
    String companyName,
    Long activeCount,
    Long pendingCount,
    Long noticeOfTerminationCount
){}
