package com.gscorp.dv1.projects.web.dto;

import java.time.LocalDate;

public record ProjectDto (

    Long id,
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Boolean active,
    Long clientId
    // Opcional: puedes agregar campos como Set<Long> employeeIds o nombres de empleados si lo necesitas
) {}