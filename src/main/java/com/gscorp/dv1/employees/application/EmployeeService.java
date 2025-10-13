package com.gscorp.dv1.employees.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.employees.infrastructure.Employee;

@Service
public interface EmployeeService {

    List<Employee> findAll ();
    
}
