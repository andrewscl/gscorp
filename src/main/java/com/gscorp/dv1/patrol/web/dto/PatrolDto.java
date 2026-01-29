package com.gscorp.dv1.patrol.web.dto;

import java.time.OffsetTime;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.patrol.infrastructure.PatrolProjection;

public record PatrolDto (
    Long id,
    String name,
    String description,
    String siteName,
    DayOfWeek dayFrom,
    DayOfWeek dayTo,
    OffsetTime startTime
){

    public static PatrolDto fromProjection(PatrolProjection p) {
        if (p == null) return null;
        return new PatrolDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getSiteName(),
            DayOfWeek.fromDayNumber(p.getDayFrom()),
            DayOfWeek.fromDayNumber(p.getDayTo()),
            p.getStartTime()
        );

    }

}
