package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;

public interface PatrolProjection {
    
    Long getId();
    String getName();
    String getDescription();
    String getSiteName();
    Integer getDayFrom();
    Integer getDayTo();
    OffsetDateTime getStartTime();

}
