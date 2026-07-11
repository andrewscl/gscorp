package com.gscorp.dv1.admin.projects.web.dto;

import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.admin.projects.infrastructure.projections.ProjectSelectProjection;

public record ProjectSelectDto (
    Long id,
    String name
){
    public static ProjectSelectDto fromEntity(Project p){
        if (p == null) return null;

        return new ProjectSelectDto(
            p.getId(),
            p.getName()
        );
    }

    public static ProjectSelectDto fromSelectProjection(ProjectSelectProjection p){
        if (p == null) return null;

        return new ProjectSelectDto(
            p.getId(),
            p.getName()
        );
    }
}
