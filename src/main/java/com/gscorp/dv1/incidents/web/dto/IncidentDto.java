package com.gscorp.dv1.incidents.web.dto;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.incidents.infrastructure.Incident;
import com.gscorp.dv1.sites.infrastructure.Site;

public class IncidentDto {

    private final Long id;
    private final String siteName;
    private final IncidentType incidentType;
    private final Priority priority;
    private final String status;
    private final String description;
    private final String photoPath;

    /**
     * Constructor público EXACTO esperado por la expresión constructor de JPQL.
     */
    public IncidentDto(
        Long id,
        String siteName,
        IncidentType incidentType,
        Priority priority,
        String status,
        String description,
        String photoPath
    ) {
        this.id = id;
        this.siteName = siteName;
        this.incidentType = incidentType;
        this.priority = priority;
        this.status = status;
        this.description = description;
        this.photoPath = photoPath;
    }

    // Getters (necesarios para serialización / uso general)
    public Long getId() { return id; }
    public String getSiteName() { return siteName; }
    public IncidentType getIncidentType() { return incidentType; }
    public Priority getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getPhotoPath() { return photoPath; }

    /**
     * Helper para crear el DTO desde la entidad.
     */
    public static IncidentDto fromEntity(Incident inc) {
        if (inc == null) return null;
        Site site = inc.getSite();

        return new IncidentDto(
            inc.getId(),
            site != null ? site.getName() : null,
            inc.getIncidentType(),
            inc.getPriority(),
            inc.getStatus() != null ? inc.getStatus().name() : null,
            inc.getDescription(),
            inc.getPhotoPath()
        );
    }

    @Override
    public String toString() {
        return "IncidentDto{" +
            "id=" + id +
            ", siteName='" + siteName + '\'' +
            ", incidentType=" + incidentType +
            ", priority=" + priority +
            ", status='" + status + '\'' +
            ", description='" + description + '\'' +
            ", photoPath='" + photoPath + '\'' +
            '}';
    }
}