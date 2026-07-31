package com.gscorp.dv1.admin.projects.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.admin.projects.infrastructure.projections.ProjectProjection;
import com.gscorp.dv1.enums.ProjectStatus;

public record ProjectDto (
    Long id,
    UUID externalId,
    String name,
    String clientName,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    ProjectStatus status,
    Boolean active
) {
    public static ProjectDto fromEntity(Project p) {
        if (p == null) return null;
        return new ProjectDto(
            p.getId(),
            p.getExternalId(),
            p.getName(),
            p.getClient().getName(),
            p.getDescription(),
            p.getStartDate(),
            p.getEndDate(),
            p.getStatus(),
            p.getActive()
        );
    }

    public static ProjectDto fromProjection(ProjectProjection p) {
        if (p == null) return null;
        return new ProjectDto(
            p.getId(),
            p.getExternalId(),
            p.getName(),
            p.getClientName(),
            p.getDescription(),
            p.getStartDate(),
            p.getEndDate(),
            p.getStatus(),
            p.getActive()
        );
    }
}
