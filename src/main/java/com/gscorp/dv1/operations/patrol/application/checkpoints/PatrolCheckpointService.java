package com.gscorp.dv1.operations.patrol.application.checkpoints;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.patrol.web.checkpoints.dto.PatrolCheckpointDto;

public interface PatrolCheckpointService {

    List<PatrolCheckpointDto> getCheckpointsByPatrolExternalId (UUID patrolExternalId);
    
}
