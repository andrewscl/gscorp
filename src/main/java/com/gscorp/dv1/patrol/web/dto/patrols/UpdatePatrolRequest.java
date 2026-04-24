package com.gscorp.dv1.patrol.web.dto.patrols;

import java.util.List;

import com.gscorp.dv1.patrol.web.dto.checkpoints.UpdatePatrolCheckpointRequest;
import com.gscorp.dv1.patrol.web.dto.schedules.UpdatePatrolScheduleRequest;

public record UpdatePatrolRequest (
    Long siteId,
    String name,
    String description,
    Integer dayFrom,
    Integer dayTo,
    Boolean active,
    List<UpdatePatrolScheduleRequest> schedules,
    List<UpdatePatrolCheckpointRequest> checkpoints
){}
