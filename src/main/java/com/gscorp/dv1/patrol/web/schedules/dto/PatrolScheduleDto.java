package com.gscorp.dv1.patrol.web.schedules.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;

public record PatrolScheduleDto (
    Long id,
    UUID externalId,
    LocalTime startTime,
    Boolean active,
    String patrolName,
    String status
){
    public static PatrolScheduleDto fromEntity (PatrolSchedule ps) {

        String name = (ps.getPatrol() != null) ?
                ps.getPatrol().getName() : "Ronda sin nombre";
        String statusLabel = (ps.getStatus() != null) ?
                ps.getStatus().getDisplayName() : "Sin estado";

        return new PatrolScheduleDto(
                    ps.getId(),
                    ps.getExternalId(),
                    ps.getStartTime(),
                    ps.getActive(),
                    name,
                    statusLabel
                    );
    }
}
