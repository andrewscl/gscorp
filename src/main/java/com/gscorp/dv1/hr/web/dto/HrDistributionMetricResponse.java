package com.gscorp.dv1.hr.web.dto;

import java.util.List;

import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatDto;

public record HrDistributionMetricResponse (
    List<CompanyEmployeesStatDto> companyEmployeesStats,
    List<ClientEmployeesStatDto> clientEmployeesStats
){}
