package com.gscorp.dv1.patrol.infrastructure.schedules;

import java.time.LocalTime;
import java.util.UUID;

public interface PatrolScheduleProjection {
    Long getId();
    UUID getExternalId();
    LocalTime getStartTime();
    Boolean getActive();
}
