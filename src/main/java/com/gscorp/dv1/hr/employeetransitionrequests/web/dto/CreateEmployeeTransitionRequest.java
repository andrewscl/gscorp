package com.gscorp.dv1.hr.employeetransitionrequests.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.enums.EmployeeRequestStatusType;
import com.gscorp.dv1.enums.TerminationReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeTransitionRequest (
    @NotNull(message = "El ID del empleado es obligatorio")
    Long employeeId,

    @NotNull(message = "El tipo de solicitud es obligatorio")
    EmployeeRequestStatusType requestType,

    @NotNull(message = "El motivo de termino es obligatorio")
    TerminationReason terminationReason,

    @NotNull(message = "La fecha de salida propuesta es obligatoria")
    LocalDate proposedExitDate,

    @Size(max = 500, message = "La justificación no puede superar los 500 caracteres")
    String reason
){}
