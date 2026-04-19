package com.gscorp.dv1.patrol.web.dto;

import java.time.LocalTime;
import java.util.UUID;

public record PatrolScheduleDto (
    UUID externalId,
    LocalTime startTime,
    Boolean active
){
    
}
