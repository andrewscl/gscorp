package com.gscorp.dv1.operations.patrol.web.patrols.dto;

import java.util.List;

import com.gscorp.dv1.operations.patrol.web.checkpoints.dto.UpdatePatrolCheckpointRequest;
import com.gscorp.dv1.operations.patrol.web.schedules.dto.UpdatePatrolScheduleRequest;

public record UpdatePatrolRequest (
    Long siteId,
    String name,
    String description,
    Integer dayFrom,
    Integer dayTo,
    Boolean active,
    List<UpdatePatrolScheduleRequest> schedules,
    List<UpdatePatrolCheckpointRequest> checkpoints
){
    
}
