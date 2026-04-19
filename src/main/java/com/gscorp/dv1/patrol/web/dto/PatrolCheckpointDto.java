package com.gscorp.dv1.patrol.web.dto;

import java.util.UUID;

public record PatrolCheckpointDto (
    UUID externalId,
    String name,
    Double latitude,
    Double longitude,
    Integer minutesToReach,
    Boolean active
){
    
}
