package com.gscorp.dv1.employees.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.employees.infrastructure.Projections.statistics.ClientEmployeesStatProjection;
import com.gscorp.dv1.employees.infrastructure.Projections.statistics.CompanyEmployeesStatProjection;
import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeStatServiceImpl implements EmployeeStatService{

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<CompanyEmployeesStatDto> getCompanyEmployeesStat() {

        List<CompanyEmployeesStatProjection> projections =
                    employeeRepository.getCompanyEmployeesStat();

        return projections
                    .stream()
                    .map(CompanyEmployeesStatDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<ClientEmployeesStatDto> getClientEmployeesStat() {

        List<ClientEmployeesStatProjection> projections =
                    employeeRepository.getClientEmployeesStat();

        return projections
                    .stream()
                    .map(ClientEmployeesStatDto::fromProjection)
                    .toList();
    }

}
