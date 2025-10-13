package com.gscorp.dv1.employees.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService{
    
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public List<Employee> findAll (){
        return employeeRepository.findAll();
    }

}
