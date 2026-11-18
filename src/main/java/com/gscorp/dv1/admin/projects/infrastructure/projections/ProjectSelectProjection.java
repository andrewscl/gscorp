package com.gscorp.dv1.admin.projects.infrastructure.projections;

import java.util.UUID;

public interface ProjectSelectProjection {
        Long getId();
        UUID getExternalId();
        String getName();
}
