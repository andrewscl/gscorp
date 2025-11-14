package com.gscorp.dv1.incidents.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.incidents.infrastructure.Incident;

public record IncidentDto (
    Long id,
    String siteName,
    String incidentType,
    String priority,
    String description,
    String photoPath,
    String status,
    OffsetDateTime openedTs,
    OffsetDateTime firstResponseTs,
    OffsetDateTime closedTs,
    Integer slaMinutes,
    OffsetDateTime createdAt

){
    public static IncidentDto fromEntity (Incident inc) {
        if (inc == null) return null;

        return new IncidentDto(
            inc.getId(),
            inc.getSite() != null ? inc.getSite().getName() : null,
            inc.getIncidentType() != null ? inc.getIncidentType().name() : null,
            inc.getPriority() != null ? inc.getPriority().name() : null,
            inc.getDescription(),
            inc.getPhotoPath(),
            inc.getStatus() != null ? inc.getStatus().name() : null,
            inc.getOpenedTs(),
            inc.getFirstResponseTs(),
            inc.getClosedTs(),
            inc.getSlaMinutes(),
            inc.getCreatedAt()
        );
    }
}
