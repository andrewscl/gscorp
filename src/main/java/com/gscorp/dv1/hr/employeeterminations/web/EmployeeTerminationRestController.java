package com.gscorp.dv1.hr.employeeterminations.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.hr.employeeterminations.application.EmployeeTerminationService;
import com.gscorp.dv1.hr.employeeterminations.web.dto.CreateEmployeeTermination;
import com.gscorp.dv1.hr.employeeterminations.web.dto.EmployeeTerminationDto;
import com.gscorp.dv1.hr.employeeterminations.web.dto.ManageEmployeeTermination;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/employee-terminations")
@RequiredArgsConstructor
public class EmployeeTerminationRestController {

    private final EmployeeTerminationService employeeTerminationService;
    
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeTerminationDto> create(
                @Valid @ModelAttribute CreateEmployeeTermination req,
                UriComponentsBuilder ucb,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        EmployeeTerminationDto saved = 
                        employeeTerminationService
                            .createEmployeeTermination(req, securityUser);
        var location = ucb.path("/api/employee-terminations/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping(value = "/manage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeTerminationDto> manage(
                @Valid @ModelAttribute ManageEmployeeTermination req,
                UriComponentsBuilder ucb,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        EmployeeTerminationDto saved = 
                        employeeTerminationService
                            .manageEmployeeTermination(req, securityUser);
        var location = ucb.path("/api/employee-terminations/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();
        return ResponseEntity.created(location).body(saved);
    }

}
