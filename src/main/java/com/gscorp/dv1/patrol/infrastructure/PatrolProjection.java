package com.gscorp.dv1.patrol.infrastructure;

public interface PatrolProjection {
    
    Long getId();
    String getName();
    String getSiteName();
    Integer getDayFrom();
    Integer getDayTo();

}
