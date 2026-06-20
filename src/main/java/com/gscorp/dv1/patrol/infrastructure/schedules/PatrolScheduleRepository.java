package com.gscorp.dv1.patrol.infrastructure.schedules;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolScheduleRepository
        extends JpaRepository<PatrolSchedule, Long> {

    List<PatrolScheduleProjection> findByPatrolId(Long patrolId);

    Optional<PatrolSchedule> findByPatrolIdAndStartTime (
        Long PatrolId, LocalTime StartTime);



/*
    Caso 1: Mismo día (ej: entre las 22:00 y las 23:59)
    Caso 2: El rango cruza a la madrugada del día siguiente (Tramo A: lo que queda de hoy)
    Caso 3: El rango cruza a la madrugada del día siguiente (Tramo B: las primeras horas de mañana)
*/

    @Query("""
        SELECT s FROM PatrolSchedule s 
        WHERE s.patrol.site.externalId = :siteExternalId 
        AND s.active = true 
        AND s.patrol.active = true 
        AND (
            (:isOvernight = false AND s.startTime >= :nowTime AND s.startTime <= :targetTime AND :currentDay BETWEEN s.patrol.dayFrom AND s.patrol.dayTo)
            OR 
            (:isOvernight = true AND s.startTime >= :nowTime AND :currentDay BETWEEN s.patrol.dayFrom AND s.patrol.dayTo)
            OR
            (:isOvernight = true AND s.startTime <= :targetTime AND :nextDay BETWEEN s.patrol.dayFrom AND s.patrol.dayTo)
        )
        ORDER BY s.startTime ASC
    """)
    List<PatrolSchedule> findNext24hSchedulesBySiteExternalId(
            @Param("siteExternalId") UUID siteExternalId,
            @Param("nowTime") LocalTime nowTime,
            @Param("targetTime") LocalTime targetTime,
            @Param("currentDay") Integer currentDay,
            @Param("nextDay") Integer nextDay,
            @Param("isOvernight") boolean isOvernight);

}
