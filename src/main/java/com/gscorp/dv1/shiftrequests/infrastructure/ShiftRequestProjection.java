package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gscorp.dv1.enums.ShiftRequestType;

public interface ShiftRequestProjection {
    
    Long getId();
    String getCode();
    Long getSiteId();
    String getSiteName();
    Long getClientAccountId();
    ShiftRequestType getType();
    LocalDate getStartDate();
    LocalDate getEndDate();
    String getStatus();
    String getDescription();
    LocalDateTime getCreatedAt();

    // Resumen ligero sobre schedules
    Integer getSchedulesCount();

}
