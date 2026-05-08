package com.gscorp.dv1.patrolexecution.application.patrolsexecution;

import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;

public interface PatrolExecutionService {

    PatrolExecutionDto createPatrolExecution (
                CreatePatrolExecutionRequest request,
                Long userId
    );


}
