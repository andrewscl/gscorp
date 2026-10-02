package com.gscorp.dv1.operations.shiftrequests.infrastructure.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZone;

public interface ShiftRequestProjection {
    
    Long getId();
    UUID getExternalId();
    String getCode();
    Long getSiteId();
    String getSiteName();
    String getShiftPatternName();
    Long projectId();
    String projectName();
    Long getClientAccountId();
    ShiftRequestType getType();
    SiteZone getSiteZone();
    LocalDate getStartDate();
    LocalDate getEndDate();
    ShiftRequestStatus getStatus();
    String getDescription();
    LocalDateTime getCreatedAt();
    Integer getSchedulesCount();

}
