package com.gscorp.dv1.employees.application;

import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;

public interface EmployeeService {

    List<Employee> findAll ();
    Employee findByIdWithUserAndProjects(Long id);
    Employee saveEmployee(Employee employee);
    Optional<Employee> findById(Long id);
    Optional<Employee> findByUsername(String username);

    Employee createEmployeeFromRequest(CreateEmployeeRequest req);

    List<Employee> findAllWithProjects();
    
}
