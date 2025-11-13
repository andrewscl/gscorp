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


  @Query("""
    select new com.gscorp.dv1.incidents.web.dto.IncidentDto(
      i.id,
      s.id,
      s.name,
      i.incidentType,
      i.priority,
      i.status.name(),
      i.openedTs,
      i.firstResponseTs,
      i.closedTs,
      i.slaMinutes,
      i.description,
      i.photoPath,
      u.id,
      u.username
    )
    from Incident i
    join i.site s
    left join i.createdBy u
    where s.client.id in :clientIds
    order by i.openedTs desc
  """)
  List<IncidentDto> findAllForClients(@Param("clientIds") List<Long> clientIds);
  
}
