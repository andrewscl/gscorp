package com.gscorp.dv1.patrol.application.patrols;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.patrol.web.patrols.dto.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.patrols.dto.PatrolDto;
import com.gscorp.dv1.patrol.web.patrols.dto.UpdatePatrolRequest;

public interface PatrolService {

    List<PatrolDto> getPatrolsByUserExternalUserId(UUID userExternalId);

    PatrolDto savePatrol (
                    CreatePatrolRequest request,
                    Long userId
    );

    PatrolDto getPatrolByExternalId(String externalId);

    PatrolDto updatePatrol (
                    String externalId,
                    UpdatePatrolRequest request,
                    Long userId
    );

    

}
