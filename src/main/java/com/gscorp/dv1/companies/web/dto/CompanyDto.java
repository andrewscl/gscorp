package com.gscorp.dv1.companies.web.dto;

import java.util.UUID;

import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.enums.CompanyStatus;

public record CompanyDto (
    Long id,
    UUID externalId,
    String name,
    String legalName,
    String taxId,
    CompanyStatus companyStatus
){
    public static CompanyDto fromEntity (Company company){
        if (company == null) return null;
        return new CompanyDto(
            company.getId(),
            company.getExternalId(),
            company.getName() != null ? company.getName().trim() : "",
            company.getLegalName() != null ? company.getLegalName().trim() : "",
            company.getTaxId() != null ? company.getTaxId().trim() : "",
            company.getStatus()
        );
    }

}
