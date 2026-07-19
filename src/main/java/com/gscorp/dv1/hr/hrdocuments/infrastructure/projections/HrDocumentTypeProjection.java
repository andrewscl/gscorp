package com.gscorp.dv1.hr.hrdocuments.infrastructure.projections;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

public interface HrDocumentTypeProjection {
    Long getId();
    UUID getExternalId();
    String getCode();
    String getName();
    Boolean getRequired();
    EmployeeStatus getStatus();
    HrProcessType getTargetProcess();
    String getCreatedBy();
    String getUpdatedBy();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
}
