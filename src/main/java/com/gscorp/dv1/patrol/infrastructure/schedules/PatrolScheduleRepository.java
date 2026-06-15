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

    @Query("""
        SELECT s FROM PatrolSchedule s 
        WHERE s.patrol.site.externalId = :siteExternalId 
        AND s.active = true 
        AND s.patrol.active = true 
        AND :currentDay BETWEEN s.patrol.dayFrom AND s.patrol.dayTo 
        ORDER BY s.startTime ASC
    """)
    List<PatrolSchedule> findTodaySchedulesBySiteExternalId(
            @Param("siteExternalId") UUID siteExternalId, 
            @Param("currentDay") Integer currentDay);

}
