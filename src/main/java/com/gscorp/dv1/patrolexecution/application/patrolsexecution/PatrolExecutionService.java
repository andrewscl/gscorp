package com.gscorp.dv1.patrolexecution.application.patrolsexecution;

import java.util.UUID;

import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.StartPatrolExecutionRequest;

public interface PatrolExecutionService {

    PatrolExecutionDto startPatrolExecution (
                        StartPatrolExecutionRequest request,
                        UUID patrolScheduleExternalId,
                        UUID userExternalId
    );

    PatrolExecutionDto endPatrolExecution(
                        CreatePatrolExecutionRequest request,
                        UUID patrolExternalId,
                        UUID userExternalId);

}
