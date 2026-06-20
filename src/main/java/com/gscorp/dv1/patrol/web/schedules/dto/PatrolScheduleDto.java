package com.gscorp.dv1.patrol.web.schedules.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.enums.PatrolScheduleStatus;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;

public record PatrolScheduleDto (
    Long id,
    UUID externalId,
    LocalTime startTime,
    Boolean active,
    String patrolName,
    PatrolScheduleStatus status
){
    public static PatrolScheduleDto fromEntity (PatrolSchedule ps) {

        String name = (ps.getPatrol() != null) ?
                ps.getPatrol().getName() : "Ronda sin nombre";

        return new PatrolScheduleDto(
                    ps.getId(),
                    ps.getExternalId(),
                    ps.getStartTime(),
                    ps.getActive(),
                    name,
                    ps.getStatus()
                    );
    }
}
