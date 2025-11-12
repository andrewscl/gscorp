package com.gscorp.dv1.incidents.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.incidents.infrastructure.Incident;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.users.infrastructure.User;

public record IncidentDto(
    Long id,
    Long siteId,
    String siteName,
    IncidentType incidentType,
    Priority priority,
    String status,
    OffsetDateTime openedTs,
    OffsetDateTime firstResponseTs,
    OffsetDateTime closedTs,
    Integer slaMinutes,
    String description,
    String photoPath,
    Long createdById,
    String createdByUsername
) {
    public static IncidentDto fromEntity(Incident inc) {
        if (inc == null) return null;
        Site site = inc.getSite();
        User createdBy = inc.getCreatedBy();

        return new IncidentDto(
            inc.getId(),
            site != null ? site.getId() : null,
            site != null ? site.getName() : null,
            inc.getIncidentType(),
            inc.getPriority(),
            inc.getStatus() != null ? inc.getStatus().name() : null,
            inc.getOpenedTs(),
            inc.getFirstResponseTs(),
            inc.getClosedTs(),
            inc.getSlaMinutes(),
            inc.getDescription(),
            inc.getPhotoPath(),
            createdBy != null ? createdBy.getId() : null,
            createdBy != null ? createdBy.getUsername() : null
        );
    }
}