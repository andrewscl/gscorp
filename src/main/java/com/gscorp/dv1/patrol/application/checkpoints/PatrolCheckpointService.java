package com.gscorp.dv1.patrol.application.checkpoints;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.patrol.web.checkpoints.dto.PatrolCheckpointDto;

public interface PatrolCheckpointService {

    List<PatrolCheckpointDto> getCheckpointsByExternalId (UUID externalIdStr);
    
}
