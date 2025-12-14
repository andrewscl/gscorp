package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ShiftRequestProjection {
    
    Long getId();
    String getCode();
    Long getSiteId();
    String getSiteName();
    Long getClientAccountId();
    String getType();
    LocalDate getStartDate();
    LocalDate getEndDate();
    String getStatus();
    String getDescription();
    LocalDateTime getCreatedAt();

    // Resumen ligero sobre schedules
    Integer getSchedulesCount();

}
