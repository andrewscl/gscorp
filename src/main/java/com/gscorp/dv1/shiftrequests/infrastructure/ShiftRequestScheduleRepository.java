package com.gscorp.dv1.shiftrequests.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShiftRequestScheduleRepository extends JpaRepository<ShiftRequestSchedule, Long> {


    @Query("""
      SELECT sc.id                AS id,
             sc.shiftRequest.id   AS shiftRequestId,
             sc.dayFrom           AS dayFrom,
             sc.dayTo             AS dayTo,
             sc.startTime         AS startTime,
             sc.endTime           AS endTime
      FROM ShiftRequestSchedule sc
      WHERE sc.shiftRequest.site.id = :siteId
        AND sc.shiftRequest.status <> 'CANCELLED'
    """)
    List<ShiftRequestScheduleProjection> findBySiteId(@Param("siteId") Long siteId);

    // Alternativa: schedules por lista de shiftRequestIds
    @Query("""
      SELECT sc.id                AS id,
             sc.shiftRequest.id   AS shiftRequestId,
             sc.dayFrom           AS dayFrom,
             sc.dayTo             AS dayTo,
             sc.startTime         AS startTime,
             sc.endTime           AS endTime
      FROM ShiftRequestSchedule sc
      WHERE sc.shiftRequest.id IN :ids
    """)
    List<ShiftRequestScheduleProjection> findByShiftRequestIds(@Param("ids") List<Long> ids);



    @Query("""
      SELECT sc.id                AS id,
            sc.shiftRequest.id   AS shiftRequestId,
            sc.dayFrom           AS dayFrom,
            sc.dayTo             AS dayTo,
            sc.startTime         AS startTime,
            sc.endTime           AS endTime,
            sc.shiftRequest.startDate AS requestStartDate,
            sc.shiftRequest.endDate   AS requestEndDate
      FROM ShiftRequestSchedule sc
      JOIN sc.shiftRequest r
      JOIN r.site s
      JOIN s.project p
      WHERE p.client.id IN :clientIds
        AND r.status <> 'CANCELLED'
    """)
    List<ShiftRequestScheduleProjection> findByClientIds(@Param("clientIds") List<Long> clientIds);

}
