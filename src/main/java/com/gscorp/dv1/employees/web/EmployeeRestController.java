package com.gscorp.dv1.employees.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeDto> createEmployee(
        @Valid @ModelAttribute CreateEmployeeRequest req,
        UriComponentsBuilder ucb
    ) {

        Employee saved = employeeService.
                                createEmployeeFromRequest(req);
        var location = ucb.path("/api/employees/{id}")
                            .buildAndExpand(saved.getId()).toUri();

        EmployeeDto dto = EmployeeDto.fromEntity(saved);

        return ResponseEntity.created(location).body(dto);

    }

}