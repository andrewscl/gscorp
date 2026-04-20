package com.gscorp.dv1.patrol.web.dto.patrols;

import java.util.List;

import com.gscorp.dv1.patrol.web.dto.checkpoints.UpdateCheckpointDto;
import com.gscorp.dv1.patrol.web.dto.schedules.UpdateScheduleDto;

public record UpdatePatrolRequest (
    Long siteId,
    String name,
    String description,
    Integer dayFrom,
    Integer dayTo,
    Boolean active,
    List<UpdateScheduleDto> schedules,
    List<UpdateCheckpointDto> checkpoints
){}
