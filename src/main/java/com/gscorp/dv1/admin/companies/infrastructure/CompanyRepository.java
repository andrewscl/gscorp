package com.gscorp.dv1.admin.companies.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.admin.companies.infrastructure.projections.CompanyProjection;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query(
        value = """
            SELECT
                c.id AS id,
                c.externalId AS externalId,
                c.name AS name,
                c.legalName AS legalName,
                c.taxId AS taxId,
                c.status AS status
            FROM Company c
            """,
        countQuery = "SELECT COUNT(c.id) FROM Company c"
    )
    Page<CompanyProjection> findAllCompanies(
        Pageable pageable
    );

    @Query(
        value = """
            SELECT
                c.id AS id,
                c.externalId AS externalId,
                c.name AS name,
                c.legalName AS legalName,
                c.taxId AS taxId,
                c.status AS status
            FROM Company c
            WHERE c.externalId = :externalId
            """
    )
    CompanyProjection findCompanyDtoByExternalId(
        @Param("externalId") UUID externalId
    );

}
