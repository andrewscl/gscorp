package com.gscorp.dv1.dashboard.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDashboardRepo extends JpaRepository<Object, Long>{

  // Serie de entradas por día (asistencia “proxy”)
  // attendance_punches.user_id -> guards.user_id -> sites -> client_id
  @Query(value = """
    SELECT to_char(ap.ts::date,'YYYY-MM-DD') AS day, COUNT(*) AS cnt
    FROM attendance_punches ap
    JOIN guards g       ON g.user_id = ap.user_id
    JOIN sites s        ON s.id = g.site_id
    WHERE s.client_id = :clientId
      AND ap.ts::date BETWEEN :from AND :to
      AND ap.action = 'IN'
    GROUP BY day
    ORDER BY day
  """, nativeQuery = true)
  List<Object[]> attendanceInByDay(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);

  // Cumplimiento de rondas = sum(completed)/sum(expected)
  // patrol_runs -> patrol_routes -> sites -> client
  @Query(value = """
    SELECT COALESCE(NULLIF(SUM(r.completed_hits),0)*1.0 / NULLIF(SUM(r.expected_hits),0), 0)
    FROM patrol_runs r
    JOIN patrol_routes pr ON pr.id = r.route_id
    JOIN sites s          ON s.id = pr.site_id
    WHERE s.client_id = :clientId
      AND r.started_ts::date BETWEEN :from AND :to
  """, nativeQuery = true)
  Double patrolCompliance(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);

  // Incidentes por día
  @Query(value = """
    SELECT to_char(i.ts::date,'YYYY-MM-DD') AS day, COUNT(*) AS cnt
    FROM incidents i
    JOIN sites s ON s.id = i.site_id
    WHERE s.client_id = :clientId
      AND i.ts::date BETWEEN :from AND :to
    GROUP BY day
    ORDER BY day
  """, nativeQuery = true)
  List<Object[]> incidentsByDay(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);

}
