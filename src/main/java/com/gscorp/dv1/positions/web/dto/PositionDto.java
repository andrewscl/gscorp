package com.gscorp.dv1.positions.web.dto;

public record PositionDto (
    Long id,
    String name
){
    public static PositionDto fromEntity(com.gscorp.dv1.positions.infrastructure.Position p) {
        if (p == null) return null;
        return new PositionDto(
            p.getId(),
            p.getName()
        );
    }
}
