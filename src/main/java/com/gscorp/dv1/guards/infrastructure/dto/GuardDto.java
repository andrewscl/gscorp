package com.gscorp.dv1.guards.infrastructure.dto;

public record GuardDto (
    Long id,
    Long userId,
    String name,
    String externalId,
    Boolean active 
){
    
}
