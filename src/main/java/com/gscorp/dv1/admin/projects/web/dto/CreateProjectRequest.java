package com.gscorp.dv1.admin.projects.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectRequest (
    UUID clientExternalId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    String description
){}
