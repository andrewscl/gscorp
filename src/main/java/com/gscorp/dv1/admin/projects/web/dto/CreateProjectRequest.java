package com.gscorp.dv1.admin.projects.web.dto;

import java.time.LocalDate;

public record CreateProjectRequest (
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Boolean active,
    Long clientId
){}
