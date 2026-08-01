package com.gscorp.dv1.admin.companies.application;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.admin.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.admin.companies.infrastructure.CompanySpecRepository;
import com.gscorp.dv1.admin.companies.infrastructure.projections.CompanyProjection;
import com.gscorp.dv1.admin.companies.infrastructure.specification.CompanySpecifications;
import com.gscorp.dv1.admin.companies.web.dto.CompanyDto;
import com.gscorp.dv1.admin.companies.web.dto.CompanySelectDto;
import com.gscorp.dv1.admin.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.admin.companies.web.dto.CreateCompanyRequest;
import com.gscorp.dv1.enums.CompanyStatus;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanySpecRepository companySpecRepo;

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

    @Transactional(readOnly = true)
    public Page <CompanyTableDto> searchCompaniesListByUserExternalId (
        UUID userExternalId, String q, CompanyStatus status, int page, int size
    ) {
        if (userExternalId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 100);
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();
        PageRequest pg = PageRequest.of(safePage, safeSize);
        Specification<Company> spec = Specification
            .where(CompanySpecifications.searchCompanies(safeQ, status))
            .and(CompanySpecifications.belongsToUser(userExternalId));
        Page<Company> companies = companySpecRepo.findAll(spec, pg);
        return companies.map(CompanyTableDto::fromEntity);
    }

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

    @Transactional(readOnly = true)
    public List<CompanySelectDto> getAllCompaniesForSelect() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(CompanySelectDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyDto findCompanyDtoByExternalId (UUID externalId) {
        CompanyProjection companyProjection = 
            companyRepository.findCompanyDtoByExternalId(externalId);
        if (companyProjection == null) {
            throw new EntityNotFoundException("Not found Company: " + externalId);
        }
        return CompanyDto.fromProjection(companyProjection);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> findCompaniesByUserExternalIdAndStatus (
                        UUID userExternalId,
                        CompanyStatus status) {
        if (userExternalId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        List<CompanyProjection> projections =
                companyRepository.findCompaniesByUserIdAndStatus(userExternalId, status);
        return projections.stream()
                    .map(CompanyDto::fromProjection)
                    .toList();
    }

}
