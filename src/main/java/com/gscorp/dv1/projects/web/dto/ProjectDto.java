package com.gscorp.dv1.projects.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.projects.infrastructure.ProjectProjection;

public record ProjectDto (
    Long id,
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Boolean active,
    Long clientId
) {
    public static ProjectDto fromEntity(Project p) {
        if (p == null) return null;
        return new ProjectDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getStartDate(),
            p.getEndDate(),
            p.getActive(),
            p.getClient() != null ? p.getClient().getId() : null
        );
    }

    public static ProjectDto fromProjection(ProjectProjection p) {
        if (p == null) return null;
        return new ProjectDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getStartDate(),
            p.getEndDate(),
            p.getActive(),
            p.getClientId()
        );
    }
}