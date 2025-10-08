// src/main/java/com/gscorp/dv1/dashboard/infrastructure/ClientDashboardRepo.java
package com.gscorp.dv1.dashboard.infrastructure;

import java.time.LocalDate;
import java.util.List;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch; // <-- importa tu entidad

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDashboardRepo extends JpaRepository<AttendancePunch, Long> {

  // Proyección limpia para series (opcional, evita Object[])
  public interface DayCount {
    String getDay();
    Long   getCnt();
  }

  // Serie entradas por día
  @Query(value = """
    SELECT to_char(ap.ts::date,'YYYY-MM-DD') AS day,
           COUNT(*)::bigint                   AS cnt
    FROM attendance_punches ap
    JOIN guards g  ON g.user_id = ap.user_id
    JOIN sites s   ON s.id = g.site_id
    WHERE s.client_id = :clientId
      AND ap.ts::date BETWEEN :from AND :to
      AND ap.action = 'IN'
    GROUP BY day
    ORDER BY day
  """, nativeQuery = true)
  List<DayCount> attendanceInByDay(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);

  // Cumplimiento de rondas (double precision en PG)
  @Query(value = """
    SELECT COALESCE(
             SUM(r.completed_hits)::double precision
             / NULLIF(SUM(r.expected_hits),0),
             0
           )::double precision
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
    SELECT to_char(i.ts::date,'YYYY-MM-DD') AS day,
           COUNT(*)::bigint                 AS cnt
    FROM incidents i
    JOIN sites s ON s.id = i.site_id
    WHERE s.client_id = :clientId
      AND i.ts::date BETWEEN :from AND :to
    GROUP BY day
    ORDER BY day
  """, nativeQuery = true)
  List<DayCount> incidentsByDay(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);
}
