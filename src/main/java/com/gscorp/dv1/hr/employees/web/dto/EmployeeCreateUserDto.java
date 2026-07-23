package com.gscorp.dv1.hr.employees.web.dto;

import java.util.Set;
import java.util.UUID;

import com.gscorp.dv1.admin.clients.web.dto.ClientSelectDto;

public record EmployeeCreateUserDto (
    Long id,
    UUID externalId,
    String name,
    String fatherSurname,
    String motherSurname,
    String email,
    String username,
    Long companyId,
    Set<ClientSelectDto> clients
){}
