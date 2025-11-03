package com.gscorp.dv1.patrol.web.dto;

public record CreatePatrolCheckpointRequest (
    Long siteId,         // id de Site (foreign key)
    Long routeId,         // id de PatrolRoute (foreign key)
    String name,          // nombre del checkpoint
    Double lat,           // latitud
    Double lon,           // longitud
    Integer orderN,       // orden/secuencia en la ruta
    Integer toleranceM
){

}
