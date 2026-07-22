package com.gscorp.dv1.hr.employeeterminations.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.enums.TerminationReason;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class ManageEmployeeTermination {
    @NotNull(message = "El ID de la solicitud es obligatorio")
    private UUID externalId;

    @NotNull(message = "El ID del empleado es obligatorio")
    private UUID employeeId;

    @NotNull(message = "El motivo de termino final es obligatorio")
    private TerminationReason finalTerminationReason;

    @NotNull(message = "La fecha de salida final es obligatoria")
    private LocalDate finalExitDate;

    @NotNull(message = "El tipo de documento es obligatorio")
    private UUID hrDocumentType;

    @NotNull(message = "El archivo es obligatorio")
    private MultipartFile file;
}
