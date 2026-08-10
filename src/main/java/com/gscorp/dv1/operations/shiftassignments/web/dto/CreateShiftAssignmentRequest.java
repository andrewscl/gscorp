package com.gscorp.dv1.operations.shiftassignments.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateShiftAssignmentRequest (
    @NotNull(message = "El sitio es obligatorio")
    UUID siteExternalId,

    @NotNull(message = "El requerimiento es obligatorio")
    UUID shiftRequestExternalId,

    @NotNull(message = "El empleado es obligatorio")
    UUID employeeExternalId,

    @NotNull(message = "El día de inicio del ciclo es obligatorio")
    @Min(value = 1, message = "El día inicial del ciclo debe ser al menos 1")
    Integer startCycleNumber,

    String notes,

    @NotNull(message = "La fecha de asignación es obligatoria")
    OffsetDateTime assignedAt,

    OffsetDateTime assignedUntil

){}

