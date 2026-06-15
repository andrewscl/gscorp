package com.gscorp.dv1.patrolexecution.application.patrolsexecution;

import java.util.UUID;

import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;

public interface PatrolExecutionService {

    PatrolExecutionDto createPatrolExecution (
                CreatePatrolExecutionRequest request,
                UUID patrolExternalId,
                UUID userExternalId
    );

}
