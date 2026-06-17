package com.gscorp.dv1.hr.web.dto;

import java.util.List;

public record HrDashboardMetricResponse (
    List<CompanyStatDto> companyStats,
    List<ClientStatDto> clientStats,
    List<CompanyUserStatDto> companyUserStats
){}
