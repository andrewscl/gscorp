package com.gscorp.dv1.admin.companies.web.dto;

import java.util.UUID;

import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.admin.companies.infrastructure.projections.CompanyProjection;
import com.gscorp.dv1.enums.CompanyStatus;

public record CompanyDto (
    Long id,
    UUID externalId,
    String name,
    String legalName,
    String taxId,
    CompanyStatus status
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

    public static CompanyDto fromProjection (CompanyProjection projection){
        if (projection == null) return null;
        return new CompanyDto(
            projection.getId(),
            projection.getExternalId(),
            projection.getName(),
            projection.getLegalName(),
            projection.getTaxId(),
            projection.getStatus()
        );
    }

}
