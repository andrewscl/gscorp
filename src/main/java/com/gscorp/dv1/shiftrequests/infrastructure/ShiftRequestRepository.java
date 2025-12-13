package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.ShiftRequestType;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{

    @Query("SELECT sr.code FROM ShiftRequest sr WHERE sr.site.id = :siteId AND sr.code LIKE CONCAT(:prefix, '%') ORDER BY sr.code DESC")
    String findLastCodeBySiteIdAndPrefix(@Param("siteId") Long siteId, @Param("prefix") String prefix);

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
                     SUM(CASE WHEN sc.startDate >= :start AND sc.startDate < :endExclusive THEN 1 ELSE 0 END) AS schedulesCount,
                     MIN(CASE WHEN sc.startDate >= :now THEN sc.startDate ELSE NULL END)         AS nextScheduleStart
              FROM ShiftRequest r
              JOIN r.site s
              JOIN s.project p
              LEFT JOIN r.shiftRequestSchedules sc
              WHERE p.client.id IN :clientIds
                     AND (:siteId IS NULL OR s.id = :siteId)
                     AND (:type IS NULL OR r.type = :type)
              GROUP BY
                     r.id, r.code, s.id, s.name, r.clientAccountId, r.type,
                     r.startDate, r.endDate, r.status, r.description, r.createdAt
              ORDER BY r.startDate DESC
              """)
       List<ShiftRequestProjection> findProjectionByUserAndDateBetween(
       @Param("clientIds") List<Long> clientIds,
       @Param("start") OffsetDateTime start,
       @Param("endExclusive") OffsetDateTime endExclusive,
       @Param("siteId") Long siteId,
       @Param("type") ShiftRequestType type,
       @Param("now") OffsetDateTime now
       );


}

