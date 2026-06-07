package com.gscorp.dv1.companies.application;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.companies.infrastructure.CompanySpecRepository;
import com.gscorp.dv1.companies.infrastructure.projections.CompanyProjection;
import com.gscorp.dv1.companies.infrastructure.specification.CompanySpecifications;
import com.gscorp.dv1.companies.web.dto.CompanyDto;
import com.gscorp.dv1.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.companies.web.dto.CreateCompanyRequest;
import com.gscorp.dv1.enums.CompanyStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private final CompanyRepository companyRepository;

    @Autowired
    private final CompanySpecRepository companySpecRepo;

    @Override
    @Transactional(readOnly = true)
    public List<Company> validateAndFindAllById(Set<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Company> companies = companyRepository.findAllById(ids);

        if (companies.size() != ids.size()) {
            throw new IllegalArgumentException("Some company IDs are invalid");
        }

        return companies;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CompanyTableDto> getAllCompaniesTableForAdmin(
        int page, int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);

        PageRequest pg = PageRequest.of(safePage, safeSize);
        Page<CompanyProjection> projections;

        projections = companyRepository.findAllCompanies(pg);
        return projections.map(CompanyTableDto::fromProjection);
    }


    @Override
    @Transactional(readOnly = true)
    public Page <CompanyTableDto> searchCompaniesTableByUserId (
        Long userId, String q, CompanyStatus status, int page, int size
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 100);
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        PageRequest pg = PageRequest.of(safePage, safeSize);

        Specification<Company> spec = Specification
            .where(CompanySpecifications.searchCompanies(safeQ, status))
            .and(CompanySpecifications.belongsToUser(userId));

        Page<Company> companies = companySpecRepo.findAll(spec, pg);

        return companies.map(CompanyTableDto::fromEntity);
    }


    @Override
    @Transactional
    public CompanyDto createCompany (
            CreateCompanyRequest request){

        Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

        String currentUser = (authentication != null) ?
                                        authentication.getName() : "SYSTEM";

        Company company = Company.builder()
                            .externalId(UUID.randomUUID())
                            .name(request.name())
                            .legalName(request.legalName())
                            .taxId(request.taxId())
                            .status(CompanyStatus.ACTIVE)
                            .createdBy(currentUser)
                            .build();

        Company savedCompany = companyRepository.save(company);

        return CompanyDto.fromEntity(savedCompany);
    }

}
