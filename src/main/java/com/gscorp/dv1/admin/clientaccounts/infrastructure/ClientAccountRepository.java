package com.gscorp.dv1.admin.clientaccounts.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {

    @Query(
        value = """
        SELECT ca
        FROM ClientAccount ca
        JOIN ca.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (ca.externalId = :externalId)
    """)
    Optional<ClientAccount> findByExternalId(
            @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
            @Param("projectIds") List<Long> projectIds,
            @Param("externalId") UUID externalId
    );

    @Query(
        value = """
        SELECT ca
        FROM ClientAccount ca
        JOIN ca.project p
        JOIN p.sites s
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (s.externalId = :siteExternalId)
    """)
    List<ClientAccount> findClientAccountsBySite(
            @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
            @Param("projectIds") List<Long> projectIds,
            @Param("siteExternalId") UUID siteExternalId
    );

    @Query(
        value = """
        SELECT ca
        FROM ClientAccount ca
        JOIN ca.project p
        JOIN p.sites s
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
    """)
    List<ClientAccount> findClientAccountsByUser(
            @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
            @Param("projectIds") List<Long> projectIds
    );

}
