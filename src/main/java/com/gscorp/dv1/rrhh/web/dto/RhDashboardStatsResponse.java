package com.gscorp.dv1.rrhh.web.dto;

import java.util.List;

public record RhDashboardStatsResponse (
    List<CompanyStatDto> companyStats,
    List<ClientStatDto> clientStats
){}
