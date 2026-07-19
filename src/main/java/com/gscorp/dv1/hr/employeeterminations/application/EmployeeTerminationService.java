package com.gscorp.dv1.hr.employeeterminations.application;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.hr.employeeterminations.web.dto.CreateEmployeeTermination;
import com.gscorp.dv1.hr.employeeterminations.web.dto.EmployeeTerminationDto;


public interface EmployeeTerminationService {

    Page<EmployeeTerminationDto> getTransitionRequestTable(
                                UUID userExternalId,
                                EmployeeTransitionStatus status,
                                int page,
                                int size);

    EmployeeTerminationDto createEmployeeTermination (
                                CreateEmployeeTermination request,
                                SecurityUser securityUser);

}
