package com.gscorp.dv1.employees.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.hr.web.dto.ClientStatDto;
import com.gscorp.dv1.hr.web.dto.CompanyStatDto;
import com.gscorp.dv1.hr.web.dto.CompanyUserStatDto;
import com.gscorp.dv1.hr.web.dto.HrDashboardMetricResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeStatServiceImpl implements EmployeeStatService{

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public HrDashboardMetricResponse getRhDashboardStats() {

        List<CompanyStatDto> companyStats = 
                        employeeRepository.getEmployeeStatsByCompany();
        List<ClientStatDto> clientStats = 
                        employeeRepository.getEmployeeStatsByClient();
        List<CompanyUserStatDto> companyUserStats =
                        employeeRepository.getCompanyUserStats();

        return new HrDashboardMetricResponse(
                        companyStats, clientStats, companyUserStats);
    }

}
