package com.gscorp.dv1.hr.employeetransitionrequests.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeRequestStatusType;
import com.gscorp.dv1.enums.EmployeeTransitionRequestStatus;
import com.gscorp.dv1.enums.TerminationReason;
import com.gscorp.dv1.hr.employeetransitionrequests.infrastructure.projections.EmployeeTransitionRequestProjection;

public record EmployeeTransitionRequestDto (
    Long id,
    UUID externalId,
    EmployeeRequestStatusType requestType,
    TerminationReason terminationReason,
    EmployeeTransitionRequestStatus status,
    LocalDate proposedExitDate,
    String description,
    String resolvedBy,
    OffsetDateTime resolvedAt,
    String createdBy,
    String updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
){
    
    public static EmployeeTransitionRequestDto fromProjection(EmployeeTransitionRequestProjection p){
        if (p == null) return null;

        return new EmployeeTransitionRequestDto(
            p.getId(),
            p.getExternalId(),
            p.getRequestType(),
            p.getTerminationReason(),
            p.getStatus(),
            p.getProposedExitDate(),
            p.getDescription(),
            p.getResolvedBy(),
            p.getResolvedAt(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

}
