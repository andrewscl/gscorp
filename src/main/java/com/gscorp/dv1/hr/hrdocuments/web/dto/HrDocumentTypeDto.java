package com.gscorp.dv1.hr.hrdocuments.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;
import com.gscorp.dv1.hr.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;

public record HrDocumentTypeDto (
    Long id,
    UUID externalId,
    String code,
    String name,
    Boolean required,
    EmployeeStatus status,
    HrProcessType targetProcess,
    String createdBy,
    String updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
){
    public static HrDocumentTypeDto fromProjection(HrDocumentTypeProjection p){
        if (p == null) return null;
        return new HrDocumentTypeDto(
            p.getId(),
            p.getExternalId(),
            p.getCode(),
            p.getName(),
            p.getRequired(),
            p.getStatus(),
            p.getTargetProcess(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
