package com.gscorp.dv1.operations.shiftrequests.infrastructure;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestProjection;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{

    @Query("SELECT sr.code FROM ShiftRequest sr WHERE sr.site.id = :siteId AND sr.code LIKE CONCAT(:prefix, '%') ORDER BY sr.code DESC")
    String findLastCodeBySiteIdAndPrefix(@Param("siteId") Long siteId, @Param("prefix") String prefix);

       // ShiftRequestRepository.java
       Optional<ShiftRequest> findFirstBySiteIdAndCodeStartingWithOrderByCodeDesc(Long siteId, String prefix);

       // Cargar ShiftRequest + Site + Schedules (filtrar por client ids recorriendo project -> client)
       @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "where st.project.client.id in :clientIds " +
           "order by sr.code")
       List<ShiftRequest> findBySiteClientIdInFetchSiteAndSchedules(@Param("clientIds") Collection<Long> clientIds);

    // Cargar todos (admin)
    @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "order by sr.code")
    List<ShiftRequest> findAllWithSiteAndSchedules();

    // Cargar uno por id con sus relaciones
    @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "where sr.id = :id")
    Optional<ShiftRequest> findByIdWithSiteAndSchedules(@Param("id") Long id);


    @Query("""
       select distinct sr
       from ShiftRequest sr
       join fetch sr.site st
       left join fetch sr.schedules sch
       where sr.id = :id
       and st.project.client.id in :clientIds
       """)
       Optional<ShiftRequest> findByIdAndSiteProjectClientIdInFetchSiteAndSchedules(@Param("id") Long id,
                                                                             @Param("clientIds") Collection<Long> clientIds);


       @Query("""
       SELECT
              r.id                                  AS id,
              r.code                                AS code,
              s.id                                  AS siteId,
              s.name                                AS siteName,
              r.clientAccountId                     AS clientAccountId,
              r.type                                AS type,
              r.startDate                           AS startDate,
              r.endDate                             AS endDate,
              r.status                              AS status,
              r.description                         AS description,
              r.createdAt                           AS createdAt,
       COUNT(sc.id)                          AS schedulesCount
       FROM ShiftRequest r
       JOIN r.site s
       JOIN s.project p
       LEFT JOIN r.schedules sc
       WHERE p.client.id IN :clientIds
              AND r.startDate >= COALESCE(:fromDate, r.startDate)
              AND r.startDate <  COALESCE(:toDateExclusive, r.startDate)
              AND s.id = COALESCE(:siteId, s.id)
              AND r.type = COALESCE(:type, r.type)
       GROUP BY
       r.id, r.code, s.id, s.name, r.clientAccountId, r.type,
       r.startDate, r.endDate, r.status, r.description, r.createdAt
       ORDER BY r.startDate DESC
       """)
       List<ShiftRequestProjection> findProjectionByUserAndDateBetween(
       @Param("clientIds") List<Long> clientIds,
       @Param("fromDate") LocalDate fromDate,
       @Param("toDateExclusive") LocalDate toDateExclusive,
       @Param("siteId") Long siteId,
       @Param("type") ShiftRequestType type
       );


       @Query ("""
              SELECT sr
              FROM ShiftRequest sr
              WHERE sr.externalId = :externalId
       """)
       Optional<ShiftRequest> findByExternalId (
                                   @Param("externalId") UUID externalId);


       @Query ("""
              SELECT sr
              FROM ShiftRequest sr
              JOIN FETCH sr.site
              WHERE sr.status = :status
       """)
       List<ShiftRequest> findAllByStatus (
                                   @Param("status") ShiftRequestStatus status);


       @Query(
              value = """
              SELECT
              sr.id                AS id,
              sr.code              AS code,
              s.id                 AS siteId,
              s.name               AS siteName,
              p.id                 AS projectId,
              p.name               AS projectName,
              sr.clientAccountId   AS clientAccountId,
              sr.type              AS type,
              sr.startDate         AS startDate,
              sr.endDate           AS endDate,
              sr.status            AS status,
              sr.description       AS description,
              sr.createdAt         AS createdAt
              FROM ShiftRequest sr
              LEFT JOIN sr.site s
              LEFT JOIN s.project p
              WHERE p.client.id IN :clientIds
              AND sr.startDate >= COALESCE(:startDate, sr.startDate)
              AND sr.startDate <  COALESCE(:endExclusiveDate, sr.startDate) 
              AND (:siteId IS NULL OR s.id = :siteId)
              AND (:projectId IS NULL OR p.id = :projectId)
              AND (:shiftRequestType IS NULL OR sr.type = :shiftRequestType)
              AND (
              (:startDate IS NULL AND :endExclusiveDate IS NULL AND sr.type = 'FIXED')
              OR 
              ((:startDate IS NOT NULL OR :endExclusiveDate IS NOT NULL) AND (:shiftRequestType IS NULL OR sr.type = :shiftRequestType))
              )
              """,
              countQuery = """
              SELECT COUNT(sr.id)
              FROM ShiftRequest sr
              LEFT JOIN sr.site s
              LEFT JOIN s.project p
              WHERE p.client.id IN :clientIds
              AND sr.startDate >= COALESCE(:startDate, sr.startDate)
              AND sr.startDate <  COALESCE(:endExclusiveDate, sr.startDate) 
              AND (:siteId IS NULL OR s.id = :siteId)
              AND (:projectId IS NULL OR p.id = :projectId)
              AND (:shiftRequestType IS NULL OR sr.type = :shiftRequestType)
              AND (
              (:startDate IS NULL AND :endExclusiveDate IS NULL AND sr.type = 'FIXED')
              OR 
              ((:startDate IS NOT NULL OR :endExclusiveDate IS NOT NULL) AND (:shiftRequestType IS NULL OR sr.type = :shiftRequestType))
              )
              """
       )
       Page<ShiftRequestProjection> findPageByClientIds(
              @Param("clientIds") List<Long> clientIds,
              @Param("startDate") OffsetDateTime startDate,
              @Param("endExclusiveDate") OffsetDateTime endExclusiveDate,
              @Param("siteId") Long siteId,
              @Param("projectId") Long projectId,
              @Param("shiftRequestType") ShiftRequestType shiftRequestType,
              Pageable pageable
       );

}
