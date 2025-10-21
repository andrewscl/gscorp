package com.gscorp.dv1.professions.web.dto;

import com.gscorp.dv1.professions.infrastructure.Profession;

public record ProfessionDto (
    Long id,
    String name
)   {
    public static ProfessionDto fromEntity(Profession p) {
        if (p == null) return null;
        return new ProfessionDto(
            p.getId(),
            p.getName()
        );
    }
}
