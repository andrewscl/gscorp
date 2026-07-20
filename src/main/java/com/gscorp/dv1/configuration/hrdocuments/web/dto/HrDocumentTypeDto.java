package com.gscorp.dv1.configuration.hrdocuments.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentType;
import com.gscorp.dv1.configuration.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

public record HrDocumentTypeDto (
    Long id,
    UUID externalId,
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

    public static HrDocumentTypeDto fromEntity(HrDocumentType e){
        if (e == null) return null;
        return new HrDocumentTypeDto(
            e.getId(),
            e.getExternalId(),
            e.getName(),
            e.getRequired(),
            e.getStatus(),
            e.getTargetProcess(),
            e.getCreatedBy(),
            e.getUpdatedBy(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

}
