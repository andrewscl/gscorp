package com.gscorp.dv1.admin.clients.web.dto;

import java.util.UUID;

import com.gscorp.dv1.enums.ClientStatus;

public record CreateClientRequest (
    String name,
    String legalName,
    String taxId,
    @jakarta.validation.constraints.Email
    String email,
    String phone,
    ClientStatus status,
    UUID companyExternalId
){}
