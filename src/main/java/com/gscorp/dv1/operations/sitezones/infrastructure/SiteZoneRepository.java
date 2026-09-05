package com.gscorp.dv1.operations.sitezones.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.infrastructure.projections.SiteZoneProjection;

@Repository 
public interface SiteZoneRepository
                        extends JpaRepository<SiteZone, Long> {

    @Query(
        value = """
        SELECT
        sz.id                       AS id,
        sz.externalId               AS externalId,
        sz.name                     AS name,
        sz.status                   AS status
        FROM SiteZone sz
        LEFT JOIN sz.site s
        LEFT JOIN s.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (:status IS NULL OR sz.status = :status)
        AND (:siteExternalId IS NULL OR s.externalId = :siteExternalId)
    """)
    List<SiteZoneProjection> findByProjectIds(
        @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
        @Param("projectIds") List<Long> projectIds,
        @Param("siteExternalId") UUID siteExternalId,        
        @Param("status") SiteZoneStatus status
    );

}

