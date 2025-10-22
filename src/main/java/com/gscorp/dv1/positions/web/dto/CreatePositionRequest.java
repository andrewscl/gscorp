package com.gscorp.dv1.positions.web.dto;

public record CreatePositionRequest (
    String name,
    String description,
    Boolean active,
    String code,
    Integer level
){}
