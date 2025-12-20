package com.gscorp.dv1.positions.web.dto;

import com.gscorp.dv1.positions.infrastructure.PositionProjection;

public record PositionDto (
    Long id,
    String name,
    String description,
    Boolean active,
    String code,
    Integer level
){
    public static PositionDto fromEntity(com.gscorp.dv1.positions.infrastructure.Position p) {
        if (p == null) return null;
        return new PositionDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getActive(),
            p.getCode(),
            p.getLevel()
        );
    }

    public static PositionDto fromProjection(PositionProjection p) {
        if (p == null) return null;
        return new PositionDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getActive(),
            p.getCode(),
            p.getLevel()
        );
    }
}
