package com.gscorp.dv1.operations.patrol.web.checkpoints.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.gscorp.dv1.operations.patrol.infrastructure.checkpoints.PatrolCheckpointProjection;

public record PatrolCheckpointDto (
    UUID externalId,
    String name,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer checkpointOrder,
    Integer stayTime,
    Integer minutesToReach,
    Boolean active
){
    public static PatrolCheckpointDto fromProjection(
        PatrolCheckpointProjection projection
    ) {
        if(projection == null){
            return null;
        }

        return new PatrolCheckpointDto(
            projection.getExternalId(),
            projection.getName(),
            projection.getDescription(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getCheckpointOrder(),
            projection.getStayTime(),
            projection.getMinutesToReach(),
            projection.getActive()
        );

    }
}
