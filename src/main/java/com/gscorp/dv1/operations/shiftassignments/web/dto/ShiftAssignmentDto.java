package com.gscorp.dv1.operations.shiftassignments.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.projections.ShiftAssignmentProjection;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestScheduleDto;

public record ShiftAssignmentDto (
    Long id,
    UUID externalId,
    UUID shiftExternalId,
    Long shiftRequestId,
    String shiftRequestCode,
    String siteName,
    UUID employeeExternalId,
    String employeeName,
    String employeeFatherSurname,
    String employeeRut,
    ShiftAssignmentStatus status,
    String notes,
    OffsetDateTime AssignedAt,
    String createdBy,
    String UpdatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<ShiftRequestScheduleDto> schedules
){
    public static ShiftAssignmentDto fromProjection(
                                ShiftAssignmentProjection p,
                                List<ShiftRequestScheduleDto> schedules){
        if(p == null) return null;
        return new ShiftAssignmentDto(
            p.getId(),
            p.getExternalId(),
            p.getShiftExternalId(),
            p.getShiftRequestId(),
            p.getShiftRequestCode(),
            p.getSiteName(),
            p.getEmployeeExternalId(),
            p.getEmployeeName(),
            p.getEmployeeFatherSurname(),
            p.getEmployeeRut(),
            p.getStatus(),
            p.getNotes(),
            p.getAssignedAt(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt(),
            schedules
        );
    }
}
