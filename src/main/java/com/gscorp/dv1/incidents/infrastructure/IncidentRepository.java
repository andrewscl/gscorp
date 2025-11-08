package com.gscorp.dv1.incidents.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>{

 interface DayCount { String getDay(); Long getCnt(); }

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
  List<DayCount> byDayForClient(
      @Param("clientId") Long clientId,
      @Param("from") LocalDate from,
      @Param("to")   LocalDate to);
    
}
