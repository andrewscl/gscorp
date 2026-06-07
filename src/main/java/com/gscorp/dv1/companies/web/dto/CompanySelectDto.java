package com.gscorp.dv1.companies.web.dto;

import com.gscorp.dv1.companies.infrastructure.Company;

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
