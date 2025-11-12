package com.gscorp.dv1.incidents.web.dto;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter @Setter
public class CreateIncidentRequest {

    @NotNull(message = "Debe selccionar un sitio")
    private Long siteId;

    @NotNull(message = "Debe seleccionar un tipo de incidente")
    private IncidentType incidentType;

    @NotNull(message = "Debe seleccionar una prioridad")
    private Priority priority;

    @Size(max = 2000, message = "description demasiado larga")
    private String description;

    private MultipartFile photoPath;

}
