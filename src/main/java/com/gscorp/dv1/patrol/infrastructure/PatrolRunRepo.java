package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolRunRepo extends JpaRepository<PatrolRun, Long>{

  /** Compliance como ratio sum(completed)/sum(expected) del cliente en rango. */
  @Query(value = """
    SELECT CASE
             WHEN COALESCE(SUM(r.expected_hits),0) = 0 THEN NULL
             ELSE SUM(r.completed_hits)::numeric / NULLIF(SUM(r.expected_hits),0)
           END
    FROM patrol_runs r
    JOIN patrol_routes pr ON pr.id = r.route_id
    JOIN sites s          ON s.id = pr.site_id
    WHERE s.client_id = :clientId
      AND r.started_ts BETWEEN :from AND :to
  """, nativeQuery = true)
  Double compliance(
      @Param("clientId") Long clientId,
      @Param("from") OffsetDateTime from,
      @Param("to")   OffsetDateTime to);

  /** Sumas para detalle KPI (si las quieres). */
  interface HitsSum { Integer getCompleted(); Integer getExpected(); }

  @Query(value = """
    SELECT COALESCE(SUM(r.completed_hits),0) AS completed,
           COALESCE(SUM(r.expected_hits),0)  AS expected
    FROM patrol_runs r
    JOIN patrol_routes pr ON pr.id = r.route_id
    JOIN sites s          ON s.id = pr.site_id
    WHERE s.client_id = :clientId
      AND r.started_ts BETWEEN :from AND :to
  """, nativeQuery = true)
  HitsSum hitsSum(
      @Param("clientId") Long clientId,
      @Param("from") OffsetDateTime from,
      @Param("to")   OffsetDateTime to);

}
