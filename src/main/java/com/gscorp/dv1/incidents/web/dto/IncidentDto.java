package com.gscorp.dv1.incidents.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.incidents.infrastructure.Incident;

import lombok.Value;

/**
 * DTO inmutable generado con Lombok.
 * @Value genera getters, equals/hashCode, toString y un constructor all-args público
 * que JPQL puede usar en la expresión constructor "new ...IncidentDto(...)".
 */
@Value
public class IncidentDto {
    Long id;
    Long siteId;
    String siteName;
    IncidentType incidentType;
    Priority priority;
    String status; // recomiendo String si en la query usas i.status.name()
    OffsetDateTime openedTs;
    Integer slaMinutes;
    String description;
    Long createdById;
    String createdByUsername;

    // helper factory si quieres mapear desde la entidad (opcional)
    public static IncidentDto fromEntity(Incident inc) {
        if (inc == null) return null;
        return new IncidentDto(
            inc.getId(),
            inc.getSite() != null ? inc.getSite().getId() : null,
            inc.getSite() != null ? inc.getSite().getName() : null,
            inc.getIncidentType(),
            inc.getPriority(),
            inc.getStatus() != null ? inc.getStatus().name() : null,
            inc.getOpenedTs(),
            inc.getSlaMinutes(),
            inc.getDescription(),
            inc.getCreatedBy() != null ? inc.getCreatedBy().getId() : null,
            inc.getCreatedBy() != null ? inc.getCreatedBy().getUsername() : null
        );
    }
}