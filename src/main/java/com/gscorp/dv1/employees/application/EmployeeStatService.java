package com.gscorp.dv1.employees.application;

import java.util.List;

import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatDto;

public interface EmployeeStatService {

    List<CompanyEmployeesStatDto> getCompanyEmployeesStat();

    List<ClientEmployeesStatDto> getClientEmployeesStat();

}
