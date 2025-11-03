package com.gscorp.dv1.patrol.web.dto;

public record PatrolCheckpointDto (
    Long id,
    Long siteId,
    String siteName,
    Long routeId,
    String routeName,
    String name,
    Double lat,
    Double lon,
    Integer orderN,
    Integer toleranceM
){
    
}
