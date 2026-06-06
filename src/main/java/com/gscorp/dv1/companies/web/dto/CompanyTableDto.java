package com.gscorp.dv1.companies.web.dto;

import java.util.UUID;

import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.companies.infrastructure.projections.CompanyProjection;
import com.gscorp.dv1.enums.CompanyStatus;

public record CompanyTableDto (
    Long id,
    UUID externalId,
    String name,
    String legalName,
    String taxId,
    CompanyStatus companyStatus

){

    public static CompanyTableDto fromEntity (Company company){
        if (company == null) return null;
        return new CompanyTableDto(
            company.getId(),
            company.getExternalId(),
            company.getName() != null ? company.getName().trim() : "",
            company.getLegalName() != null ? company.getLegalName().trim() : "",
            company.getTaxId() != null ? company.getTaxId().trim() : "",
            company.getStatus()
        );
    }

    public static CompanyTableDto fromProjection (CompanyProjection cp){
        if (cp == null) return null;
        return new CompanyTableDto(
            cp.getId(),
            cp.getExternalId(),
            cp.getName() != null ? cp.getName().trim() : "",
            cp.getLegalName() != null ? cp.getLegalName().trim() : "",
            cp.getTaxId() != null ? cp.getTaxId().trim() : "",
            cp.getStatus()
        );
    }

}
