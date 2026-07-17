package com.gscorp.dv1.hr.employees.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeEditDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeTableDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.hr.employees.web.dto.request.CreateEmployeeRequest;
import com.gscorp.dv1.hr.employees.web.dto.request.UpdateEmployeeRequest;
import com.gscorp.dv1.users.infrastructure.User;

public interface EmployeeService {

    List<Employee> findAll ();

    EmployeeEditDto findByExternalIdEditEmployee(UUID externalId);

    EmployeeViewDto findByExternalIdViewEmployee(UUID externalId);

    EmployeeViewDto findByIdViewEmployee(Long id);

    Employee saveEmployee(Employee employee);

    Optional<Employee> findById(Long id);
    
    Optional<Employee> findByUsername(String username);

    Employee createEmployeeFromRequest(CreateEmployeeRequest req);

    List<Employee> findAllWithProjects();

    List<Employee> findAllUnassignedEmployees();

    List<Employee> findAllWithUserAndProjectsAndPosition();

    EmployeeSelectDto findEmployeeByUserId(Long userId);

    EmployeeSelectDto findEmployeeByUserExternalId(UUID userExternalId);

    List<EmployeeSelectDto> getAllEmployeesSelectDto();

    Page<EmployeeTableDto> getEmployeeTable(
                UUID userExternalId,
                String q,
                EmployeeStatus status,
                String userStatusStr,
                int page,
                int size);

    Optional<EmployeeViewDto> updateEmployee(
                        UUID externalId, UpdateEmployeeRequest req);

    EmployeeSelectDto findEmployeeSelectDtoById(Long id);

    Employee validateAndAssignUser (Long employeeId, User user);

    List<EmployeeSelectDto> findByStatus (
                                EmployeeStatus status); 

}
