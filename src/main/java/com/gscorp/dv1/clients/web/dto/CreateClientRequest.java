package com.gscorp.dv1.clients.web.dto;

//DTO entrada
public record CreateClientRequest (
    @jakarta.validation.constraints.NotBlank
    String name,
    String legalName,
    String taxId,
    @jakarta.validation.constraints.Email
    String contactEmail,
    String contactPhone,
    Boolean active
){}
