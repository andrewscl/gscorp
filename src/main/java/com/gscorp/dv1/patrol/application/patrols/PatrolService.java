package com.gscorp.dv1.patrol.application.patrols;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.patrol.web.dto.patrols.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.patrols.PatrolDto;
import com.gscorp.dv1.patrol.web.dto.patrols.UpdatePatrolRequest;

public interface PatrolService {

    List<PatrolDto> getPatrolsByUserExtarnalUserId(UUID userExternalId);

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
