package com.gscorp.dv1.positions.infrastructure;

public interface PositionProjection {
    Long getId();
    String getName();
    String getDescription();
    Boolean getActive();
    String getCode();
    Integer getLevel();

}
