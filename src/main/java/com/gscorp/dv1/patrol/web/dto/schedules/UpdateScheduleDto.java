package com.gscorp.dv1.patrol.web.dto.schedules;

import java.time.LocalTime;

public record UpdateScheduleDto (
    LocalTime startTime,
    Boolean active
){}
