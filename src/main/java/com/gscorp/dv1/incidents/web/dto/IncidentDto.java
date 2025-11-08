package com.gscorp.dv1.incidents.web.dto;

import com.gscorp.dv1.incidents.infrastructure.Incident;

public record IncidentDto (
    Long id,
    Long siteId,
    Long typeId,
    String status,
    String openedTs,
    String firstResponseTs,
    String closedTs,
    Integer slaMinutes
){
    public static IncidentDto fromEntity(Incident inc) {
        if (inc == null) return null;
        return new IncidentDto(
            inc.getId(),
            inc.getSite() != null ? inc.getSite().getId() : null,
            inc.getType() != null ? inc.getType().getId() : null,
            inc.getStatus() != null ? inc.getStatus().name() : null,
            inc.getOpenedTs() != null ? inc.getOpenedTs().toString() : null,
            inc.getFirstResponseTs() != null ? inc.getFirstResponseTs().toString() : null,
            inc.getClosedTs() != null ? inc.getClosedTs().toString() : null,
            inc.getSlaMinutes()
        );
    }
}