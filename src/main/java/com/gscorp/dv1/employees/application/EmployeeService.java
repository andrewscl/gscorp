package com.gscorp.dv1.employees.application;

import java.util.List;

import com.gscorp.dv1.employees.infrastructure.Employee;

public interface EmployeeService {

    List<Employee> findAll ();
    Employee findByIdWithUserAndProjects(Long id);
    Employee saveEmployee(Employee employee);
    
}
