package com.gscorp.dv1.clients.web.dto;

import com.gscorp.dv1.enums.ClientStatus;

public record CreateClientRequest (
    String name,
    String legalName,
    String taxId,
    @jakarta.validation.constraints.Email
    String email,
    String phone,
    ClientStatus status,
    Long companyId
){}
