package com.gscorp.dv1.hr.employeeterminations.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.enums.TerminationReason;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.EmployeeTermination;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.projections.EmployeeTerminationProjection;

public record EmployeeTerminationDto (
    Long id,
    UUID externalId,
    TerminationReason terminationReason,
    EmployeeTransitionStatus status,
    LocalDate proposedExitDate,
    String description,
    String resolvedBy,
    OffsetDateTime resolvedAt,
    String createdBy,
    String updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
){
    public static EmployeeTerminationDto fromProjection(EmployeeTerminationProjection p){
        if (p == null) return null;
        return new EmployeeTerminationDto(
            p.getId(),
            p.getExternalId(),
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

    public static EmployeeTerminationDto fromEntity(EmployeeTermination p){
        if (p == null) return null;

        return new EmployeeTerminationDto(
            p.getId(),
            p.getExternalId(),
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
