package com.gscorp.dv1.incidents.web.dto;

import com.gscorp.dv1.incidents.infrastructure.Incident;

public record IncidentDto (
    Long id,
    String incidentType,
    String priority,
    String description,
    String photoPath,
    String status

){
    public static IncidentDto fromEntity (Incident inc) {
        if (inc == null) return null;

        return new IncidentDto(
            inc.getId(),
            inc.getIncidentType() != null ? inc.getIncidentType().name() : null,
            inc.getPriority() != null ? inc.getPriority().name() : null,
            inc.getDescription(),
            inc.getPhotoPath(),
            inc.getStatus() != null ? inc.getStatus().name() : null
        );
    }
}
