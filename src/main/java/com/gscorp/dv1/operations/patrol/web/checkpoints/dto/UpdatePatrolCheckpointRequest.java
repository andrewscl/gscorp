package com.gscorp.dv1.operations.patrol.web.checkpoints.dto;

import java.math.BigDecimal;

public record UpdatePatrolCheckpointRequest (
    String externalId,
    String name,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer checkpointOrder,
    Integer stayTime,
    Integer minutesToReach,
    Boolean active,
    Boolean deleted
){}