package com.gscorp.dv1.employees.application;

import java.util.List;

import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.EmployeesStatusSummaryDto;

public interface EmployeeStatService {

    List<CompanyEmployeesStatusSummaryDto> getCompanyEmployeesStatusSummary();

    List<ClientEmployeesStatusSummaryDto> getClientEmployeesStatusSummary();

    List<EmployeesStatusSummaryDto> getEmployeesStatusSummary();

}
