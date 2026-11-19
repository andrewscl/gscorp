package com.gscorp.dv1.operations.shiftassignments.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignmentRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
import com.gscorp.dv1.operations.shifts.infrastructure.ShiftRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShiftAssignmentProcessor {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Transactional
    public void processShiftsForAssignment (
                    ShiftAssignment assignment,
                    ZoneId zoneId
    ) {
        LocalDate startDate = assignment.getAssignedAt().toLocalDate();
        List<Shift> shifts = shiftRepository
                .findByShiftRequestAndShiftDateGreaterThanEqual(
                    assignment.getShiftRequest(), startDate);
        if (shifts.isEmpty()) return;
        LocalDate lastShiftDate = shifts.get(shifts.size() - 1).getShiftDate();
        LocalDate endDate = (assignment.getAssignedUntil() != null)
            ? assignment.getAssignedUntil().toLocalDate()
            : lastShiftDate;
        
        if(assignment.getAssignedUntil() == null){
            assignment.setAssignedUntil(lastShiftDate.atStartOfDay(zoneId).toOffsetDateTime());
            shiftAssignmentRepository.save(assignment);
        }
        var pattern = assignment.getShiftRequest().getShiftPattern();
        Long totalCycleDays = pattern.getTotalDays();   
        Long workDays = pattern.getWorkDays();
        List<Shift> assignedShifts = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now(zoneId);
        for (Shift shift : shifts) {
            if (shift.getShiftDate().isAfter(endDate)) break;
            // Solo actualizar turnos futuros
            if (!shift.getEndTs().isAfter(now)) continue;
            // No reiniciar turnos que tuvieron marcaciones.
            if (shift.getStartExecutionTs() != null
                    || shift.getEndExecutionTs() != null
                    || shift.getStatus() == ShiftStatus.IN_PROGRESS
                    || shift.getStatus() == ShiftStatus.COMPLETED
                    || shift.getStatus() == ShiftStatus.CANCELLED) continue;
            ShiftAssignment shiftAssignment = shift.getAssignment();
            if (shiftAssignment != null && !shiftAssignment.getId().equals(assignment.getId())) continue;
            long daysElapsed = ChronoUnit.DAYS.between(
                    startDate, shift.getShiftDate());
            long currentCycleDay =
                ((daysElapsed + (assignment.getStartCycleNumber() - 1)) %
                totalCycleDays)
                + 1;
            if (currentCycleDay <= workDays) {
                shift.setAssignment(assignment);
                shift.setStatus(ShiftStatus.PLANNED);
                shift.setCycleDayNumber((int) currentCycleDay);
                assignedShifts.add(shift);
            }
        }
        shiftRepository.saveAll(assignedShifts);
    }

    @Transactional
    public void processAssignmentsForShiftRequest(ShiftRequest request, ZoneId zoneId) {
    // Busca todas las asignaciones activas vinculadas a este requerimiento de turno
    List<ShiftAssignment> activeAssignments = shiftAssignmentRepository
        .findByShiftRequestAndStatus(request, ShiftAssignmentStatus.ASSIGNED);

    for (ShiftAssignment assignment : activeAssignments) {
        processShiftsForAssignment(assignment, zoneId);
    }
}

}
