package com.gscorp.dv1.hr.web.dto;

import java.util.List;

import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.EmployeesStatusSummaryDto;

public record HrDistributionMetricResponse (
    List<CompanyEmployeesStatusSummaryDto> companyEmployeesStatusSummary,
    List<ClientEmployeesStatusSummaryDto> clientEmployeesStatusSummary,
    List<EmployeesStatusSummaryDto> employeeStatusSummary
){}
