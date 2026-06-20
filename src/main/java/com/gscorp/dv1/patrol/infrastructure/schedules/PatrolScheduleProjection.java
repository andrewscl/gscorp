package com.gscorp.dv1.patrol.infrastructure.schedules;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.enums.PatrolScheduleStatus;

public interface PatrolScheduleProjection {
    Long getId();
    UUID getExternalId();
    LocalTime getStartTime();
    Boolean getActive();
    PatrolScheduleStatus getStatus();
}
