package com.gscorp.dv1.patrol.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PatrolCheckpointDto (
    UUID externalId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer minutesToReach,
    Boolean active
){
    
}
