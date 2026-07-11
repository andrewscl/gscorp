package com.gscorp.dv1.admin.companies.web.dto;

public record CreateCompanyRequest (
    String name,
    String legalName,
    String taxId

){}
