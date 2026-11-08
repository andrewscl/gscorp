package com.gscorp.dv1.admin.companies.web.dto;

import com.gscorp.dv1.admin.companies.infrastructure.Company;
import java.util.UUID;

public record CompanySelectDto (
    Long id,
    UUID externalId,
    String name
){
    public static CompanySelectDto fromEntity(Company company){
        if(company == null) return null;

        return new CompanySelectDto(
            company.getId(),
            company.getExternalId(),
            company.getName()
        );
    }
}
