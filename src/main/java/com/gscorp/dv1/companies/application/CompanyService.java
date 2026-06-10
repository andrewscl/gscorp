package com.gscorp.dv1.companies.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.companies.web.dto.CompanyDto;
import com.gscorp.dv1.companies.web.dto.CompanySelectDto;
import com.gscorp.dv1.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.companies.web.dto.CreateCompanyRequest;
import com.gscorp.dv1.enums.CompanyStatus;

public interface CompanyService {

    List<Company> validateAndFindAllById(Set<Long> ids);

    Page<CompanyTableDto> getAllCompaniesTableForAdmin(
        int page, int size);

    Page<CompanyTableDto> searchCompaniesTableByUserId(
        Long userId, String q, CompanyStatus status, int page, int size);

    CompanyDto createCompany (CreateCompanyRequest request);

    List<CompanySelectDto> getAllCompaniesForSelect();
    
    CompanyDto findCompanyDtoByExternalId (UUID externalId);

}
