package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;
import java.util.UUID;

public interface PatrolProjection {
    
    Long getId();
    UUID getExternalId();
    String getName();
    String getDescription();
    String getSiteName();
    Integer getDayFrom();
    Integer getDayTo();
    Boolean getActive();
    
    //Proyecciones anidadas
    List<PatrolScheduleProjection> getSchedules();
    List<PatrolCheckpointProjection> getCheckpoints();
}
