package com.gscorp.dv1.operations.shiftassignments.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignmentRepository;
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
        for (Shift shift : shifts) {
            if (shift.getShiftDate().isAfter(endDate)) break;
            long daysElapsed = ChronoUnit.DAYS.between(
                    startDate, shift.getShiftDate());
            long currentCycleDay =
                ((daysElapsed + (assignment.getStartCycleNumber() - 1)) %
                totalCycleDays)
                + 1;
            if (currentCycleDay <= workDays) {
                shift.setAssignment(assignment);
                assignedShifts.add(shift);
            }
        }
        shiftRepository.saveAll(assignedShifts);
    }
    
}
