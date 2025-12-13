package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.OffsetDateTime;

public interface ShiftRequestProjection {
    
    Long getId();
    String getCode();
    Long getSiteId();
    String getSiteName();
    Long getClientAccountId();
    String getType();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();
    String getStatus();
    String getDescription();
    OffsetDateTime getCreatedAt();

    // Resumen ligero sobre schedules
    Integer getSchedulesCount();
    OffsetDateTime getNextScheduleStart();

}
