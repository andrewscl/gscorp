package com.gscorp.dv1.hr.hrdocs.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.hr.hrdocs.infrastructure.projections.HumanResourcesDocumentProjection;

public record HumanResourcesDocumentDto (
    Long id,
    UUID externalId,
    String fileUrl,
    String createdBy,
    String updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Long employeeId,
    UUID employeeExternalId,
    String employeeName,
    String employeeFatherSurname,
    String employeeRut,
    Long employeeTerminationId,
    UUID employeeTerminationExternalId,
    Long hrDocumentTypeId,
    UUID hrDocumentTypeExternalId,
    String hrDocumentTypeName
){
    public static HumanResourcesDocumentDto fromProjection(HumanResourcesDocumentProjection p){
        if (p == null) return null;
        return new HumanResourcesDocumentDto (
            p.getId(),
            p.getExternalId(),
            p.getFileUrl(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt(),
            p.getEmployeeId(),
            p.getEmployeeExternalId(),
            p.getEmployeeName(),
            p.getEmployeeFatherSurname(),
            p.getEmployeeRut(),
            p.getEmployeeTerminationId(),
            p.getEmployeeTerminationExternalId(),
            p.getHrDocumentTypeId(),
            p.getHrDocumentTypeExternalId(),
            p.getHrDocumentTypeName()
        );
    }
}
