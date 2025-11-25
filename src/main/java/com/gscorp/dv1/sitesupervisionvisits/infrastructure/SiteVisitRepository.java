package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;

@Repository
public interface SiteVisitRepository
        extends JpaRepository<SiteVisit, Long> {

    @EntityGraph(attributePaths = {"employee", "site"})
    @Query("select v from SiteSupervisionVisit v")
    List<SiteVisit> findAllWithEmployeeAndSite();

    @EntityGraph(attributePaths = {"employee", "site"})
    @Query("select v from SiteSupervisionVisit v where v.id = :id")
    Optional<SiteVisit> findByIdWithEmployeeAndSite(Long id);

    @Query("select v from SiteSupervisionVisit v " +
        "where v.site.project.client.id = :clientId " +
        "and v.visitDateTime between :fromDate and :toDate")
    List<SiteVisit> findByClientIdAndDateBetween(
        @Param("clientId") Long clientId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );

    @Query("select count(v) from SiteSupervisionVisit v " +
        "where v.site.project.client.id = :clientId " +
        "and v.visitDateTime >= :fromDate and v.visitDateTime < :toDate")
    long countByClientIdAndDateBetween(@Param("clientId") Long clientId,
                                    @Param("fromDate") OffsetDateTime fromDate,
                                    @Param("toDate") OffsetDateTime toDate);

    @Query("""
    SELECT new com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto(
        v.id,
        e.id,
        e.name,
        s.id,
        s.name,
        v.visitDateTime,
        v.latitude,
        v.longitude,
        v.description,
        v.photoPath,
        v.videoPath
    )
    FROM SiteSupervisionVisit v
    JOIN v.employee e
    JOIN v.site s
    JOIN s.project p
    WHERE p.client.id = :clientId
      AND v.visitDateTime BETWEEN :fromDate AND :toDate
    """)
    List<SiteSupervisionVisitDto> findDtoByClientIdAndDateBetween(
        @Param("clientId") Long clientId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );

    @Query("""
    SELECT new com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto(
        s.id, s.name, COUNT(v)
    )
    FROM SiteSupervisionVisit v
    JOIN v.site s
    JOIN s.project p
    WHERE p.client.id = :clientId
      AND v.visitDateTime BETWEEN :fromDate AND :toDate
    GROUP BY s.id, s.name
    ORDER BY COUNT(v) DESC
    """)
    List<SiteVisitCountDto> findVisitsCountBySite(
        @Param("clientId") Long clientId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate
    );


    @Query(value = """
      WITH ag AS (
        SELECT
          sv.site_id,
          to_char(sv.visit_date_time AT TIME ZONE :tz, 'HH24') AS hour,
          COUNT(*) AS cnt
        FROM site_supervision_visits sv
        JOIN sites s ON s.id = sv.site_id
        JOIN project p ON p.id = s.project_id
        WHERE p.client_id IN (:clientIds)
          AND sv.visit_date_time >= :from
          AND sv.visit_date_time <  :to
        GROUP BY sv.site_id, hour
      )
      SELECT a.site_id    AS siteId,
            COALESCE(s.name, '') AS siteName,
            a.hour       AS hour,
            a.cnt        AS "count"
      FROM ag a
      LEFT JOIN sites s ON s.id = a.site_id
      ORDER BY a.site_id, a.hour
      """, nativeQuery = true)
    List<SiteVisitHourlyCountProjection> findByClientIdsAndDateAndHourlyBetween(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("tz") String tz,
        @Param("clientIds") List<Long> clientIds
    );


    /**
     * Cuenta visitas cuyo site -> project -> client está en clientIds
     * y cuyo timestamp (ej. 'ts') está entre from (inclusive) y to (exclusive).
     *
     * Ajusta 'v.ts' por el nombre real del campo de fecha en tu entidad SiteSupervisionVisit
     * si usa otro nombre (p.ej. 'visitedAt', 'eventAt', etc.).
     */
  @Query("select count(v) " +
        "from SiteSupervisionVisit v " +
        "where v.site.project.client.id in :clientIds " +
        "  and v.visitDateTime >= :from and v.visitDateTime < :to")
  long countByClientIdsAndTsBetween(
          @Param("clientIds") List<Long> clientIds,
          @Param("from") OffsetDateTime from,
          @Param("to") OffsetDateTime to);



    /**
     * Devuelve entidades SiteSupervisionVisit en el rango pedido.
     * Usamos JOIN FETCH v.site para evitar N+1 al acceder a site en el mapping.
     */
    @Query("""
        SELECT v
        FROM SiteSupervisionVisit v
        JOIN FETCH v.site s
        JOIN s.project p
        WHERE p.client.id IN :clientIds
          AND v.visitDateTime >= :fromDate
          AND v.visitDateTime <  :toDate
    """)
    List<SiteVisit> findByClientIdsAndDateBetween(
            @Param("clientIds") List<Long> clientIds,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate
    );

  

}
