package com.gscorp.dv1.patrol.application.checkpoints;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.patrol.web.dto.checkpoints.PatrolCheckpointDto;

public interface PatrolCheckpointService {

    List<PatrolCheckpointDto> getCheckpointsByExternalId (UUID externalIdStr);
    
}
