package com.gscorp.dv1.professions.web.dto;

import com.gscorp.dv1.professions.infrastructure.Profession;

public record ProfessionDto (
    Long id,
    String name,
    String description,
    String code,
    Boolean active,
    String category,
    Integer level
)   {
    public static ProfessionDto fromEntity(Profession p) {
        if (p == null) return null;
        return new ProfessionDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getCode(),
            p.getActive(),
            p.getCategory(),
            p.getLevel()
        );
    }
}
