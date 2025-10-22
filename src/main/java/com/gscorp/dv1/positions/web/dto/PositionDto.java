package com.gscorp.dv1.positions.web.dto;

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
}
