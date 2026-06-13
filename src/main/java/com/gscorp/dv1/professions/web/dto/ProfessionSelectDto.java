package com.gscorp.dv1.professions.web.dto;

import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.infrastructure.projections.ProfessionSelectProjection;

public record ProfessionSelectDto (
    Long id,
    String name
){
    public static ProfessionSelectDto fromEntity(Profession p){
        if(p == null) return null;

        return new ProfessionSelectDto(
            p.getId(),
            p.getName()
        );
    }

    public static ProfessionSelectDto fromProjection(ProfessionSelectProjection p){
        if(p == null) return null;

        return new ProfessionSelectDto(
            p.getId(),
            p.getName()
        );
    }
}
