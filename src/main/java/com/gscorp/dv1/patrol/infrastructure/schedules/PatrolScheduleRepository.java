package com.gscorp.dv1.patrol.infrastructure.schedules;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolScheduleRepository
        extends JpaRepository<PatrolSchedule, Long> {

    List<PatrolScheduleProjection> findByPatrolId(Long patrolId);

}
