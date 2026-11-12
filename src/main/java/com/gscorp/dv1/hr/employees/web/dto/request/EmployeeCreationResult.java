package com.gscorp.dv1.hr.employees.web.dto.request;

import java.util.UUID;

import com.gscorp.dv1.hr.employees.web.dto.EmployeeDto;

public record EmployeeCreationResult (
    UUID externalId, EmployeeDto employee
){}
