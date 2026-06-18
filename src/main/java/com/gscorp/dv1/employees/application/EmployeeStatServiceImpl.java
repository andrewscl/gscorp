package com.gscorp.dv1.employees.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.employees.infrastructure.Projections.statistics.ClientEmployeesStatusSummaryProjection;
import com.gscorp.dv1.employees.infrastructure.Projections.statistics.CompanyEmployeesStatusSummaryProjection;
import com.gscorp.dv1.employees.infrastructure.Projections.statistics.EmployeesStatusSummaryProjection;
import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.EmployeesStatusSummaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeStatServiceImpl implements EmployeeStatService{

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<CompanyEmployeesStatusSummaryDto> getCompanyEmployeesStatusSummary() {

        List<CompanyEmployeesStatusSummaryProjection> projections =
                    employeeRepository.getCompanyEmployeesStat();

        return projections
                    .stream()
                    .map(CompanyEmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<ClientEmployeesStatusSummaryDto> getClientEmployeesStatusSummary() {

        List<ClientEmployeesStatusSummaryProjection> projections =
                    employeeRepository.getClientEmployeesStat();

        return projections
                    .stream()
                    .map(ClientEmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<EmployeesStatusSummaryDto> getEmployeesStatusSummary() {

        List<EmployeesStatusSummaryProjection> projections =
                    employeeRepository.getEmployeesStatusSummary();

        return projections
                    .stream()
                    .map(EmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }

}
