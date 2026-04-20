package com.gscorp.dv1.patrol.infrastructure.patrols;

import java.util.UUID;

public interface PatrolProjection {
    
    Long getId();
    UUID getExternalId();
    String getName();
    String getDescription();
    Long getSiteId();
    String getSiteName();
    Integer getDayFrom();
    Integer getDayTo();
    Boolean getActive();
  
}
