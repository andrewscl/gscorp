package com.gscorp.dv1.operations.patrol.web.schedules.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.operations.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.operations.patrol.infrastructure.schedules.PatrolScheduleProjection;

public record PatrolScheduleDto (
    Long id,
    UUID externalId,
    LocalTime startTime,
    Boolean active,
    String patrolName,
    String status,
    UUID patrolExternalId,
    Long patrolId
){

    public static PatrolScheduleDto fromEntity (PatrolSchedule ps) {
        String name = (ps.getPatrol() != null) ? ps.getPatrol().getName() : "Ronda sin nombre";
        String statusLabel = (ps.getStatus() != null) ? ps.getStatus().getDisplayName() : "Sin estado";
        UUID pExtId = (ps.getPatrol() != null) ? ps.getPatrol().getExternalId() : null;
        Long patrolId = (ps.getPatrol() != null) ? ps.getPatrol().getId() : null;

        return new PatrolScheduleDto(
                    ps.getId(),
                    ps.getExternalId(),
                    ps.getStartTime(),
                    ps.getActive(),
                    name,
                    statusLabel,
                    pExtId,
                    patrolId
                    );
    }


    public static PatrolScheduleDto fromProjection
                                        (PatrolScheduleProjection psp){
        if( psp == null) return null;

        String name = (psp.getPatrolName() != null) ?
                psp.getPatrolName() : "Ronda sin nombre";
        String statusLabel = (psp.getStatus() != null) ?
                psp.getStatus().getDisplayName() : "Sin estado";

        return new PatrolScheduleDto(
                    psp.getId(),
                    psp.getExternalId(),
                    psp.getStartTime(),
                    psp.getActive(),
                    name,
                    statusLabel,
                    psp.getPatrolExternalId(),
                    psp.getPatrolId()
                    );
    }

}
