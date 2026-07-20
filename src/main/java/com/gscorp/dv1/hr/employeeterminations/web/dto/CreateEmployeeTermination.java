package com.gscorp.dv1.hr.employeeterminations.web.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentType;
import com.gscorp.dv1.enums.TerminationReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class CreateEmployeeTermination {

    @NotNull(message = "El ID del empleado es obligatorio")
    private Long employeeId;

    @NotNull(message = "El motivo de termino es obligatorio")
    private TerminationReason terminationReason;

    @NotNull(message = "La fecha de salida propuesta es obligatoria")
    private LocalDate proposedExitDate;

    @Size(max = 500, message = "La justificación no puede superar los 500 caracteres")
    private String description;

    private HrDocumentType hrDocumentType;

    private MultipartFile file;
}
