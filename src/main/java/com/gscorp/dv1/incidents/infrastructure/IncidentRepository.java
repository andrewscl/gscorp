package com.gscorp.dv1.incidents.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.incidents.web.dto.IncidentDto;

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

   /**
     * Devuelve todos los IncidentDto asociados a los clients cuyo owner/usuario tiene id = :userId.
     * Ajusta 'c.owner' según el nombre real del campo en Client que referencia al User.
     */
    @Query("""
    SELECT DISTINCT new com.gscorp.dv1.incidents.web.dto.IncidentDto(
        i.id,
        s.name,
        CONCAT(i.incidentType, ''),
        CONCAT(i.priority, ''),
        i.description,
        i.photoPath,
        CONCAT(i.status, ''),
        i.openedTs,
        i.firstResponseTs,
        i.closedTs,
        i.slaMinutes,
        i.createdAt
    )
    FROM Incident i
    JOIN i.site s
    JOIN s.project p
    JOIN p.client c
    JOIN c.users u
    WHERE u.id = :userId
    """)
    List<IncidentDto> findIncidentsDtoByUserId(@Param("userId") Long userId);
  
}
