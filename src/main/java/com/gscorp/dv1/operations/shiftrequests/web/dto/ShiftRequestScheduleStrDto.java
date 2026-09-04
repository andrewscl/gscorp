package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestSchedule;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;

public record ShiftRequestScheduleStrDto (
    Long shiftRequestId,
    String dayFrom,
    String dayTo,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    LocalDate requestStartDate,
    LocalDate requestEndDate
){
    public static ShiftRequestScheduleStrDto fromEntity(ShiftRequestSchedule s) {
        if (s == null) return null;
        return new ShiftRequestScheduleStrDto(
            s.getShiftRequest().getId(),
            s.getDayFrom().getDisplayNameInSpanish(),
            s.getDayTo().getDisplayNameInSpanish(),
            s.getStartTime(),
            s.getEndTime(),
            s.getLunchTime(),
            s.getShiftRequest().getStartDate(),
            s.getShiftRequest().getEndDate()
        );
    }

    public static ShiftRequestScheduleStrDto
                                fromProjection(ShiftRequestScheduleProjection sp) {
        if (sp == null) return null;
        return new ShiftRequestScheduleStrDto(
            sp.getShiftRequestId(),
            sp.getDayFrom().getDisplayNameInSpanish(),
            sp.getDayTo().getDisplayNameInSpanish(),
            sp.getStartTime(),
            sp.getEndTime(),
            sp.getLunchTime(),
            sp.getRequestStartDate(),
            sp.getRequestEndDate()
        );
    }

    public String toDisplayString() {
        String days = dayFrom.equalsIgnoreCase(dayTo)
            ? dayFrom
            : dayFrom + " - " + dayTo;
        String hours = (startTime != null && endTime != null)
            ? String.format("%s - %s", startTime, endTime)
            : "";
        return days + " (" + hours + ") ";
    }

}
