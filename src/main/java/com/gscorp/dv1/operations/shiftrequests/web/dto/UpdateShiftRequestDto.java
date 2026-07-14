package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.enums.ShiftRequestStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShiftRequestDto (
    @NotNull(message = "El estado es requerido.")
    ShiftRequestStatus status,

    @NotNull(message = "La fecha de inicio es requerida.")
    LocalDate startDate,

    @NotNull(message = "La fecha de termino es requerida.")
    LocalDate endDate,

    @Size(max = 500, message = "La descripción no puede superar los 500 carateres.")
    String description
){}
