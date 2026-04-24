package com.gscorp.dv1.patrol.application.patrols;

import java.util.List;

import com.gscorp.dv1.patrol.web.dto.patrols.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.patrols.PatrolDto;
import com.gscorp.dv1.patrol.web.dto.patrols.UpdatePatrolRequest;

public interface PatrolService {

    List<PatrolDto> getPatrolsByUserId(Long userId);

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
