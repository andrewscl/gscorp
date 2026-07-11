package com.gscorp.dv1.admin.companies.web.dto;

import com.gscorp.dv1.admin.companies.infrastructure.Company;

public record CompanySelectDto (
    Long id,
    String name
){
    public static CompanySelectDto fromEntity(Company company){
        if(company == null) return null;

        return new CompanySelectDto(
            company.getId(),
            company.getName()
        );
    }
}
