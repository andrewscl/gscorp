package com.gscorp.dv1.admin.projects.infrastructure.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ProjectStatus;

public interface ProjectProjection {

    Long getId();
    UUID getExternalId();
    String getName();
    String getClientName();
    String getDescription();
    LocalDate getStartDate();
    LocalDate getEndDate();
    ProjectStatus getStatus();
    Boolean getActive();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getCreatedBy();
    String getUpdatedBy();

}
