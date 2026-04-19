package com.gscorp.dv1.patrol.web.dto;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.patrol.infrastructure.PatrolProjection;

public record PatrolDto (
    Long id,
    UUID externalId,
    String name,
    String description,
    String siteName,
    DayOfWeek dayFrom,
    DayOfWeek dayTo,
    Boolean active,

    List<PatrolScheduleDto> schedules,
    List<PatrolCheckpointDto> checkpoints
){

    public static PatrolDto fromProjection(PatrolProjection p) {
        if (p == null) return null;

        // Mapeo de horarios
        List<PatrolScheduleDto> scheduleList =
            p.getSchedules() == null ? List.of(): p.getSchedules().stream()
            .map(s -> new PatrolScheduleDto(
                s.getExternalId(),
                s.getStartTime(),
                s.getActive()
            ))
            .toList();

        // Mapeo de puntos de control
        List<PatrolCheckpointDto> checkpointList =
            p.getCheckpoints() == null ? List.of(): p.getCheckpoints().stream()
            .map(c -> new PatrolCheckpointDto(
                c.getExternalId(),
                c.getName(),
                c.getLatitude(),
                c.getLongitude(),
                c.getMinutesToReach(),
                c.getActive()
            ))
            .toList();


        return new PatrolDto(
            p.getId(),
            p.getExternalId(),
            p.getName(),
            p.getDescription(),
            p.getSiteName(),
            DayOfWeek.fromDayNumber(p.getDayFrom()),
            DayOfWeek.fromDayNumber(p.getDayTo()),
            p.getActive(),
            scheduleList,
            checkpointList
        );
    }

}
