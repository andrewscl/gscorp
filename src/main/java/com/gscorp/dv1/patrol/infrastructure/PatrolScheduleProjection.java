package com.gscorp.dv1.patrol.infrastructure;

import java.time.LocalTime;
import java.util.UUID;

public interface PatrolScheduleProjection {
    UUID getExternalId();
    LocalTime getStartTime();
    Boolean getActive();
}
