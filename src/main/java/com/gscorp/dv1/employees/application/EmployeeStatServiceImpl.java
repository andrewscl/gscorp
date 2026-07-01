package com.gscorp.dv1.employees.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.employees.web.dto.statistics.ClientEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.EmployeesStatusSummaryDto;
import com.gscorp.dv1.employees.web.dto.statistics.CompanyEmployeesUserStatusSummaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeStatServiceImpl implements EmployeeStatService{

    private final EmployeeRepository employeeRepository;
    private final ClientService clientService;

    @Transactional(readOnly = true)
    public List<CompanyEmployeesStatusSummaryDto> getCompanyEmployeesStatusSummary(
                UUID userExternalId) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
        return List.of();
        }

        return employeeRepository.getCompanyEmployeesStat(clientIds)
                    .stream()
                    .map(CompanyEmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<ClientEmployeesStatusSummaryDto> getClientEmployeesStatusSummary(
                UUID userExternalId) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
        return List.of();
        }

        return employeeRepository.getClientEmployeesStat(clientIds)
                    .stream()
                    .map(ClientEmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<EmployeesStatusSummaryDto> getEmployeesStatusSummary(
                UUID userExternalId) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
        return List.of();
        }

        return employeeRepository.getEmployeesStatusSummary(clientIds)
                    .stream()
                    .map(EmployeesStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<CompanyEmployeesUserStatusSummaryDto> getEmployeesUserStatusSummary(
                UUID userExternalId){

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
        return List.of();
        }

        return employeeRepository.findEmployeesUserStatusSummary(clientIds)
                    .stream()
                    .map(CompanyEmployeesUserStatusSummaryDto::fromProjection)
                    .toList();
    }


}
