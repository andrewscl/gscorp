package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.DayOfWeek;

public interface PatrolProjection {
    
    Long getId();
    String getName();
    String getDescription();
    String getSiteName();
    DayOfWeek getDayFrom();
    DayOfWeek getDayTo();
    OffsetDateTime getStartTime();

}
