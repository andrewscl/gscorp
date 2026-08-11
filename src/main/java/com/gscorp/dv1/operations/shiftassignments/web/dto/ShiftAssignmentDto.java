package com.gscorp.dv1.operations.shiftassignments.web.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.projections.ShiftAssignmentProjection;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestScheduleDto;

public record ShiftAssignmentDto (
    Long id,
    UUID externalId,
    String projectName,
    String siteName,
    UUID shiftRequestExternalId,
    Long shiftRequestId,
    String shiftRequestCode,
    String shiftPatternName,
    UUID employeeExternalId,
    String employeeFullName,
    String employeeRut,
    ShiftAssignmentStatus status,
    String notes,
    OffsetDateTime assignedAt,
    OffsetDateTime assignedUntil,
    Integer startCycleNumber,
    String createdBy,
    String updatedBy,
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
            p.getProjectName(),
            p.getSiteName(),
            p.getShiftRequestExternalId(),
            p.getShiftRequestId(),
            p.getShiftRequestCode(),
            p.getShiftPatternName(),
            p.getEmployeeExternalId(),
            p.getEmployeeName(),
            p.getEmployeeRut(),
            p.getStatus(),
            p.getNotes(),
            p.getAssignedAt(),
            p.getAssignedUntil(),
            p.getStartCycleNumber(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt(),
            schedules
        );
    }

    public static ShiftAssignmentDto fromEntity(
                                ShiftAssignment p,
                                List<ShiftRequestScheduleDto> schedules){
        if(p == null) return null;

        UUID employeeExternalId = null;
        String employeeRut = null;
        String employeeFullName = null;
        if(p.getEmployee() != null) {
            employeeExternalId = p.getEmployee().getExternalId();
            employeeRut = p.getEmployee().getRut();
            String name = p.getEmployee().getName() != null 
                        ? p.getEmployee().getName() : "-" ;
            String surname = p.getEmployee().getFatherSurname() != null 
                        ? p.getEmployee().getFatherSurname() : "-" ;
            employeeFullName = (name + " " + surname).trim();
            if(employeeFullName.isEmpty()) employeeFullName = null;
        }
        
        return new ShiftAssignmentDto(
            p.getId(),
            p.getExternalId(),
            p.getShiftRequest().getSite().getProject().getName(),
            p.getShiftRequest().getSite().getName(),
            p.getShiftRequest().getExternalId(),
            p.getShiftRequest().getId(),
            p.getShiftRequest().getCode(),
            p.getShiftRequest().getShiftPattern().getName(),
            employeeExternalId,
            employeeFullName,
            employeeRut,
            p.getStatus(),
            p.getNotes(),
            p.getAssignedAt(),
            p.getAssignedUntil(),
            p.getStartCycleNumber(),
            p.getCreatedBy(),
            p.getUpdatedBy(),
            p.getCreatedAt(),
            p.getUpdatedAt(),
            schedules
        );
    }

    // Sobrecarga sin schedules si no se rqeuieren
    public static ShiftAssignmentDto fromEntity(ShiftAssignment p) {
        return fromEntity(p, Collections.emptyList());
    }
}
