package com.gscorp.dv1.companies.web.dto;

public record CreateCompanyRequest (
    String name,
    String legalName,
    String taxId

){}
