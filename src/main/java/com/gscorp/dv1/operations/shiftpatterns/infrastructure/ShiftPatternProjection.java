package com.gscorp.dv1.operations.shiftpatterns.infrastructure;

import java.util.UUID;

public interface ShiftPatternProjection {
    Long getId();
    UUID getExternalId();
    String getName();
    String getDescription();
    Long getWorkDays();
    Long getRestDays();
    String getCode();
}
