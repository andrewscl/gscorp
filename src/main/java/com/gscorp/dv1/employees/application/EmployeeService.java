package com.gscorp.dv1.employees.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeTableProjection;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeEditDto;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;

public interface EmployeeService {

    List<Employee> findAll ();

    EmployeeEditDto findByIdWithUserAndProjects(Long id);

    List<Long> findProjectIdsByEmployeeId(Long employeeId);

    Employee saveEmployee(Employee employee);

    Optional<Employee> findById(Long id);
    
    Optional<Employee> findByUsername(String username);

    Employee createEmployeeFromRequest(CreateEmployeeRequest req);

    List<Employee> findAllWithProjects();

    List<Employee> findAllUnassignedEmployees();

    List<Employee> findAllWithUserAndProjectsAndPosition();

    EmployeeSelectDto findEmployeeByUserId(Long userId);

    Page<EmployeeTableProjection> getEmployeeTable(
                        Long userId, String q, Boolean active, int page, int size);
    
}
