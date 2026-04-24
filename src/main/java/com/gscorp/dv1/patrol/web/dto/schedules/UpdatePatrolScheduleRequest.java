package com.gscorp.dv1.patrol.web.dto.schedules;

import java.time.LocalTime;

public record UpdatePatrolScheduleRequest (
    LocalTime startTime,
    Boolean active
){}
