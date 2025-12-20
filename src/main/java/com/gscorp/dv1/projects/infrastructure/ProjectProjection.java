package com.gscorp.dv1.projects.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectProjection {

    Long getId();
    String getName();
    String getDescription();
    LocalDate getStartDate();
    LocalDate getEndDate();
    Boolean getActive();
    
    // Expose client id / name from the associated Client (may be null)
    @Value("#{target.client != null ? target.client.id : null}")
    Long getClientId();

    @Value("#{target.client != null ? target.client.name : null}")
    String getClientName();

    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

}
