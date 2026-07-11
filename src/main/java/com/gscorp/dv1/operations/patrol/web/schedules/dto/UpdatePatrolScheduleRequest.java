package com.gscorp.dv1.operations.patrol.web.schedules.dto;

import java.time.LocalTime;

public record UpdatePatrolScheduleRequest (
    LocalTime startTime,
    Boolean active
){}
