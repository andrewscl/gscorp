package com.gscorp.dv1.patrol.web.dto.patrols;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointProjection;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolProjection;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleProjection;
import com.gscorp.dv1.patrol.web.dto.checkpoints.PatrolCheckpointDto;
import com.gscorp.dv1.patrol.web.dto.schedules.PatrolScheduleDto;

public record PatrolDto (
    Long id,
    UUID externalId,
    String name,
    String description,
    Long siteId,
    String siteName,
    DayOfWeek dayFrom,
    DayOfWeek dayTo,
    Boolean active,
    List<PatrolScheduleDto> schedules,
    List<PatrolCheckpointDto> checkpoints
){

    public static PatrolDto fromProjection(
            PatrolProjection p,
            List<PatrolScheduleProjection> schedules,
            List<PatrolCheckpointProjection> checkpoints) {

        if (p == null) return null;

        List<PatrolScheduleDto> scheduleDtos = schedules.stream()
                .map(s -> new PatrolScheduleDto(
                        s.getExternalId(),
                        s.getStartTime(),
                        s.getActive()
                ))
                .toList();

        List<PatrolCheckpointDto> checkpointDtos = checkpoints.stream()
                .map(c -> new PatrolCheckpointDto(
                        c.getExternalId(),
                        c.getName(),
                        c.getLatitude(),
                        c.getLongitude(),
                        c.getCheckpointOrder(),
                        c.getStayTime(),
                        c.getMinutesToReach(),
                        c.getActive()
                ))
                .toList();

        return new PatrolDto(
            p.getId(),
            p.getExternalId(),
            p.getName(),
            p.getDescription(),
            p.getSiteId(),
            p.getSiteName(),
            DayOfWeek.fromDayNumber(p.getDayFrom()),
            DayOfWeek.fromDayNumber(p.getDayTo()),
            p.getActive(),
            scheduleDtos,
            checkpointDtos
        );
    }

    public static PatrolDto fromProjection(PatrolProjection p) {
        return fromProjection(p, List.of(), List.of());
    }

}
