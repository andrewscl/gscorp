package com.gscorp.dv1.patrol.web.schedules.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;

public record PatrolScheduleDto (
    Long id,
    UUID externalId,
    LocalTime startTime,
    Boolean active
){
    public static PatrolScheduleDto fromEntity (PatrolSchedule ps) {
        return new PatrolScheduleDto(
                    ps.getId(),
                    ps.getExternalId(),
                    ps.getStartTime(),
                    ps.getActive()
                    );
    }
}
