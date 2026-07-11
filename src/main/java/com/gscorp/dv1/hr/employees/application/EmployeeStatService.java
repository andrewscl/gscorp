package com.gscorp.dv1.hr.employees.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.hr.employees.web.dto.statistics.ClientEmployeesStatusSummaryDto;
import com.gscorp.dv1.hr.employees.web.dto.statistics.CompanyEmployeesStatusSummaryDto;
import com.gscorp.dv1.hr.employees.web.dto.statistics.CompanyEmployeesUserStatusSummaryDto;
import com.gscorp.dv1.hr.employees.web.dto.statistics.EmployeesStatusSummaryDto;

public interface EmployeeStatService {

    List<CompanyEmployeesStatusSummaryDto>
                        getCompanyEmployeesStatusSummary(UUID userExternalId);

    List<ClientEmployeesStatusSummaryDto>
                        getClientEmployeesStatusSummary(UUID userExternalId);

    List<EmployeesStatusSummaryDto>
                        getEmployeesStatusSummary(UUID userExternalId);

    List<CompanyEmployeesUserStatusSummaryDto>
                        getEmployeesUserStatusSummary(UUID userExternalId);

}
