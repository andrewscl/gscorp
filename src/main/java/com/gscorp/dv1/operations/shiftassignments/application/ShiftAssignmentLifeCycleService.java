package com.gscorp.dv1.operations.shiftassignments.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignmentRepository;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
import com.gscorp.dv1.operations.shifts.infrastructure.ShiftRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentLifeCycleService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ZoneResolver zoneResolver;
    private final ShiftRepository shiftRepository;

    @Transactional
    public void closeAssignment (
            UUID userExternalId, UUID assignmentExternalId,
            ShiftAssignmentStatus status, LocalDate endAssignmentDate,
            String username, String reason, String zoneId
    ) {
        if (status == null)
            throw new IllegalArgumentException("El estado (status) es requerido para cerrar la asignación.");
        if (status != ShiftAssignmentStatus.CANCELLED && status != ShiftAssignmentStatus.FINISHED)
            throw new IllegalArgumentException("El estado destino debe ser CANCELLED o FINISHED.");
        if (endAssignmentDate == null)
            throw new IllegalArgumentException("La fecha de término (endAssignmentDate) es requerida para cerrar la asignación.");
        ShiftAssignment assignment =
            shiftAssignmentRepository.findByExternalId(assignmentExternalId)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada."));
        if (assignment.getStatus() == status) return;
        if (assignment.getStatus() == ShiftAssignmentStatus.CANCELLED || assignment.getStatus() == ShiftAssignmentStatus.FINISHED){
            throw new IllegalArgumentException("No se puede cancelar una asignación que ya está " + assignment.getStatus());
        }
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, zoneId);
        ZoneId targetZone = zoneResult.zoneId();
        OffsetDateTime endAssignmentDateOffset = endAssignmentDate.atTime(LocalTime.MAX).atZone(targetZone).toOffsetDateTime();
        assignment.setStatus(status);
        assignment.setAssignedUntil(endAssignmentDateOffset);
        if (reason != null && !reason.isBlank()) {
            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() + " | " : "";
            assignment.setNotes(existingNotes + status.getDisplayName() + ": " + reason);
        }
        shiftAssignmentRepository.save(assignment);
        List<Shift> orphanShifts = shiftRepository
                    .findByAssignmentAndShiftDateGreaterThan(assignment, endAssignmentDate);
        for (Shift shift : orphanShifts) {
            shift.setAssignment(null);
            shift.setCycleDayNumber(null);
            shift.setStatus(ShiftStatus.UNPLANNED);
        }
        shiftRepository.saveAll(orphanShifts); 
    }

}
