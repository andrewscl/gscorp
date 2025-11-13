package com.gscorp.dv1.incidents.web.dto;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.incidents.infrastructure.Incident;

public record IncidentSelectDto (
    Long id,
    IncidentType incidentType,
    Priority priority
) {

    public static IncidentSelectDto fromEntity (Incident inc) {
        if (inc == null) return null;

        return new IncidentSelectDto(
            inc.getId(),
            inc.getIncidentType(),
            inc.getPriority()
        );
    }

}

