package com.gscorp.dv1.operations.shiftassignments.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateShiftAssignmentRequest (
    @NotNull(message = "El sitio es obligatorio")
    UUID siteExternalId,

    @NotNull(message = "El turno es obligatorio")
    UUID shiftExternalId,

    @NotNull(message = "El empleado es obligatorio")
    UUID employeeExternalId,

    String notes,

    @NotNull(message = "La fecha de asignación es obligatoria")
    OffsetDateTime assignedAt

){}

