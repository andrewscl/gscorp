package com.gscorp.dv1.operations.shiftassignments.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;

import jakarta.validation.constraints.NotNull;

public record CloseShiftAssignmentRequest (
    @NotNull(message = "El estado destino es obligatorio")
    ShiftAssignmentStatus status,

    @NotNull(message = "La fecha de término es obligatoria")
    LocalDate assignmentEndDate,

    String reason

){}

