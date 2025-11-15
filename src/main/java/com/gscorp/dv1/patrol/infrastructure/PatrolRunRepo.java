package com.gscorp.dv1.patrol.infrastructure;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolRunRepo extends JpaRepository<PatrolRun, Long>{

// fragmento recomendado
@Query(value = """
  SELECT CASE
           WHEN COALESCE(SUM(r.expected_hits),0) = 0 THEN NULL
           ELSE SUM(r.completed_hits)::numeric / NULLIF(SUM(r.expected_hits),0)
         END
  FROM patrol_runs r
  JOIN patrol_routes pr ON pr.id = r.route_id
  JOIN sites s          ON s.id = pr.site_id
  WHERE s.client_id IN (:clientIds)
    AND r.started_ts BETWEEN :from AND :to
""", nativeQuery = true)
BigDecimal complianceForClients(
    @Param("clientIds") List<Long> clientIds,
    @Param("from") OffsetDateTime from,
    @Param("to")   OffsetDateTime to);

interface HitsSum { Long getCompleted(); Long getExpected(); }

@Query(value = """
  SELECT COALESCE(SUM(r.completed_hits),0) AS completed,
         COALESCE(SUM(r.expected_hits),0)  AS expected
  FROM patrol_runs r
  JOIN patrol_routes pr ON pr.id = r.route_id
  JOIN sites s          ON s.id = pr.site_id
  WHERE s.client_id IN (:clientIds)
    AND r.started_ts BETWEEN :from AND :to
""", nativeQuery = true)
HitsSum hitsSumForClients(
    @Param("clientIds") List<Long> clientIds,
    @Param("from") OffsetDateTime from,
    @Param("to")   OffsetDateTime to);

  /**
   * Proyección para resultados horarios por site.
   * siteId   -> id del sitio
   * siteName -> nombre del sitio (COALESCE a '' en la query)
   * hour     -> "HH" string (00..23)
   * cnt      -> cantidad de runs en esa hora
   */
  interface HourlySiteCount {
    Long getSiteId();
    String getSiteName();
    String getHour();
    Long getCnt();
  }

  /**
   * Conteos horarios de patrol runs agrupados por site y hora (bucket en tz).
   * Usa r.started_ts como timestamp de agrupamiento.
   */
  @Query(value = """
    SELECT
      pr.site_id                                   AS siteId,
      COALESCE(s.name, '')                         AS siteName,
      to_char( (EXTRACT(hour FROM (r.started_ts AT TIME ZONE :tz)))::int, 'FM00') AS hour,
      COUNT(*)                                     AS cnt
    FROM patrol_runs r
    JOIN patrol_routes pr ON pr.id = r.route_id
    LEFT JOIN sites s ON s.id = pr.site_id
    WHERE s.client_id IN (:clientIds)
      AND r.started_ts >= :from
      AND r.started_ts <  :to
    GROUP BY pr.site_id, s.name, (EXTRACT(hour FROM (r.started_ts AT TIME ZONE :tz)))::int
    ORDER BY pr.site_id, hour
    """, nativeQuery = true)
  List<HourlySiteCount> findHourlySiteCountsForRange(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("tz") String tz,
      @Param("clientIds") List<Long> clientIds
  );


}
