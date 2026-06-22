package com.gscorp.dv1.patrol.infrastructure.checkpoints;

import java.math.BigDecimal;
import java.util.UUID;

public interface PatrolCheckpointProjection {
    UUID getExternalId();
    String getName();
    String getDescription();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    Integer getCheckpointOrder();
    Integer getStayTime();
    Integer getMinutesToReach();
    Boolean getActive();
}
