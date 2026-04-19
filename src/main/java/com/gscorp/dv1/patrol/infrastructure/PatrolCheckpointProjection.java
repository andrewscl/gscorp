package com.gscorp.dv1.patrol.infrastructure;

import java.util.UUID;

public interface PatrolCheckpointProjection {
    UUID getExternalId();
    String getName();
    Double getLatitude();
    Double getLongitude();
    Integer getMinutesToReach();
    Boolean getActive();
}
