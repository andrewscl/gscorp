package com.gscorp.dv1.patrol.web.dto.checkpoints;

import java.math.BigDecimal;

public record UpdatePatrolCheckpointRequest (
    String externalId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer checkpointOrder,
    Integer stayTime,
    Integer minutesToReach,
    Boolean active
){}